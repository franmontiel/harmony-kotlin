package com.worldreader.core.domain.repository;

import com.worldreader.core.common.deprecated.callback.CompletionCallback;
import com.worldreader.core.domain.model.BookMetadata;
import com.worldreader.core.domain.model.StreamingResource;

public interface StreamingBookRepository {

  String TAG = StreamingBookRepository.class.getSimpleName();

  String KEY_LATEST = "latest";
  String CONTENT_OPF_LOCATION_PATH = "content/META-INF/container.xml";

  void retrieveBookMetadata(final String bookId, final boolean forceRefreshBookMetadata,
      final CompletionCallback<BookMetadata> callback);

  BookMetadata getBookMetadata(final String bookId);

  StreamingResource getBookResource(final String id, final BookMetadata bookMetadata,
      final String resource) throws Throwable;

  StreamingResource getBookResourceFastAccess(final String id, final String resource);

  boolean deleteBookResource(final String bookId, final String resource);

}
