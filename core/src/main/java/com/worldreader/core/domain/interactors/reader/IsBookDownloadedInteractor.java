package com.worldreader.core.domain.interactors.reader;

import com.google.common.util.concurrent.ListenableFuture;
import com.worldreader.core.common.deprecated.error.ErrorCore;
import com.worldreader.core.domain.deprecated.DomainCallback;

public interface IsBookDownloadedInteractor {

  void execute(String bookId, DomainCallback<Boolean, ErrorCore<?>> callback);

  ListenableFuture<Boolean> execute(final String bookId);
}