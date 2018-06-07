package com.worldreader.core.domain.interactors.reader;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.worldreader.core.common.deprecated.error.ErrorCore;
import com.worldreader.core.domain.deprecated.DomainCallback;
import com.worldreader.core.domain.model.Book;

import java.util.*;

public interface GetBooksDownloadedFullInformationInteractor {

  void execute(DomainCallback<List<Book>, ErrorCore<?>> callback);

  ListenableFuture<Optional<List<Book>>> execute();

}