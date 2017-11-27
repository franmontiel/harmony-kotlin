package com.worldreader.core.application.helper;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.worldreader.core.application.di.annotation.PerActivity;
import com.worldreader.core.domain.thread.MainThread;

import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

@PerActivity
public class CancelableInteractorHandler {

  private List<WeakReference<CancelableFutureCallback<?>>> callbacks;
  private final MainThread mainThread;

  @Inject
  public CancelableInteractorHandler(MainThread mainThread) {
    this.mainThread = mainThread;
    this.callbacks = new ArrayList<>();
  }

  public <T> void addCallbackMainThread(ListenableFuture<T> listenableFuture, CancelableFutureCallback<T> callback) {
    callbacks.add(new WeakReference<CancelableFutureCallback<?>>(callback));
    Futures.addCallback(listenableFuture, callback, mainThread.getMainThreadExecutor());
  }

  public <T> void addCallback(ListenableFuture<T> listenableFuture, CancelableFutureCallback<T> callback) {
    callbacks.add(new WeakReference<CancelableFutureCallback<?>>(callback));
    Futures.addCallback(listenableFuture, callback);
  }

  public <T> void addCallback(ListenableFuture<T> listenableFuture, CancelableFutureCallback<T> callback, Executor executor) {
    callbacks.add(new WeakReference<CancelableFutureCallback<?>>(callback));
    Futures.addCallback(listenableFuture, callback, executor);
  }

  public void cancel() {
    final Iterator<WeakReference<CancelableFutureCallback<?>>> iterator = callbacks.iterator();

    while (iterator.hasNext()) {
      final CancelableFutureCallback callback = iterator.next().get();
      if (callback != null) {
        callback.cancel();
      } else {
        iterator.remove();
      }
    }
  }

}