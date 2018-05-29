package com.worldreader.core.domain.interactors.reader;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import com.worldreader.core.common.callback.Callback;
import com.worldreader.core.common.deprecated.error.ErrorCore;
import com.worldreader.core.concurrency.SafeRunnable;
import com.worldreader.core.domain.deprecated.AbstractInteractor;
import com.worldreader.core.domain.deprecated.DomainBackgroundCallback;
import com.worldreader.core.domain.deprecated.DomainCallback;
import com.worldreader.core.domain.deprecated.executor.InteractorExecutor;
import com.worldreader.core.domain.model.BookMetadata;
import com.worldreader.core.domain.repository.StreamingBookRepository;
import com.worldreader.core.domain.thread.MainThread;

import javax.inject.Inject;
import java.util.concurrent.*;

public class GetBookMetadataInteractorImpl extends AbstractInteractor<BookMetadata, ErrorCore<?>> implements GetBookMetadataInteractor {

  private final ListeningExecutorService executorService;
  private final StreamingBookRepository streamingBookRepository;

  private String bookId;
  private String version;
  private boolean forceBookMetadataRefresh;

  private DomainCallback<BookMetadata, ErrorCore<?>> callback;
  private DomainBackgroundCallback<BookMetadata, ErrorCore<?>> backgroundCallback;

  @Inject public GetBookMetadataInteractorImpl(InteractorExecutor executor, MainThread mainThread, ListeningExecutorService executor1,
      StreamingBookRepository streamingBookRepository) {
    super(executor, mainThread);
    this.executorService = executor1;
    this.streamingBookRepository = streamingBookRepository;
  }

  @Override public void execute(final String bookId, final String version, final DomainBackgroundCallback<BookMetadata, ErrorCore<?>> callback) {
    this.bookId = bookId;
    this.version = version;
    this.forceBookMetadataRefresh = false;
    this.backgroundCallback = callback;
    this.executor.run(this);
  }

  //@Override public void execute(String bookId, boolean forceRefreshBookMetadata, DomainBackgroundCallback<BookMetadata, ErrorCore<?>> callback) {
  //  this.bookId = bookId;
  //  this.version = "latest";
  //  this.forceBookMetadataRefresh = forceRefreshBookMetadata;
  //  this.backgroundCallback = callback;
  //  this.executor.run(this);
  //}

  @Override public void execute(final String bookId, final String version, final boolean forceRefreshBookMetadata,
      final DomainBackgroundCallback<BookMetadata, ErrorCore<?>> callback) {
    this.bookId = bookId;
    this.version = version;
    this.backgroundCallback = callback;
    this.forceBookMetadataRefresh = forceRefreshBookMetadata;
    this.executor.run(this);
  }

  @Override public ListenableFuture<BookMetadata> execute(final String bookId, final String version) {
    final SettableFuture<BookMetadata> future = SettableFuture.create();
    executorService.execute(getInteractorCallable(bookId, version, false, future));
    return future;
  }

  @Override
  public ListenableFuture<BookMetadata> execute(final String bookId, final String version, final boolean forceRefreshBookMetadata, final Executor executor) {
    final SettableFuture<BookMetadata> future = SettableFuture.create();
    executor.execute(getInteractorCallable(bookId, version, forceRefreshBookMetadata, future));
    return future;
  }

  private Runnable getInteractorCallable(final String bookId, final String version, final boolean forceBookMetadataRefresh,
      final SettableFuture<BookMetadata> future) {
    return new SafeRunnable() {
      @Override protected void safeRun() throws Throwable {
        streamingBookRepository.retrieveBookMetadata(bookId, version, forceBookMetadataRefresh, new Callback<BookMetadata>() {
          @Override public void onSuccess(BookMetadata bookMetadata) {
            future.set(bookMetadata);
          }

          @Override public void onError(Throwable e) {
            future.setException(e);
          }
        });
      }

      @Override protected void onExceptionThrown(Throwable t) {
        future.setException(t);
      }
    };
  }

  @Override public void run() {
    streamingBookRepository.retrieveBookMetadata(this.bookId, this.version, this.forceBookMetadataRefresh, new Callback<BookMetadata>() {
      @Override public void onSuccess(final BookMetadata result) {
        if (backgroundCallback != null) {
          backgroundCallback.onSuccess(result);
        } else {
          performSuccessCallback(callback, result);
        }

        callback = null;
        backgroundCallback = null;
      }

      @Override public void onError(Throwable e) {
        if (backgroundCallback != null) {
          backgroundCallback.onError(ErrorCore.of(e));
        } else {
          performErrorCallback(callback, ErrorCore.of(e));
        }

        callback = null;
        backgroundCallback = null;

        bookId = null;
        version = null;
        forceBookMetadataRefresh = false;
      }
    });
  }

}