package com.worldreader.core.domain.interactors.referrer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.worldreader.core.application.di.annotation.PerActivity;
import com.worldreader.core.datasource.ReferrerDataSource;
import com.worldreader.core.domain.model.Referrer;

import javax.inject.Inject;
import java.util.concurrent.*;

@PerActivity public class SaveReferrerInteractor {

  private final ListeningExecutorService executorService;
  private final ReferrerDataSource referrerRepository;

  @Inject public SaveReferrerInteractor(ListeningExecutorService executorService, ReferrerDataSource referrerRepository) {
    this.executorService = executorService;
    this.referrerRepository = referrerRepository;
  }

  public ListenableFuture<Void> execute(final Referrer referrer) {
    return this.executorService.submit(new Callable<Void>() {
      @Override public Void call() throws Exception {
        referrerRepository.put(referrer);
        return null;
      }
    });
  }

}
