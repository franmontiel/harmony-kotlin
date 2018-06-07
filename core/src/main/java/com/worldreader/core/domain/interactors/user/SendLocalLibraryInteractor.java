package com.worldreader.core.domain.interactors.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.worldreader.core.application.di.annotation.PerActivity;
import com.worldreader.core.common.callback.Callback;
import com.worldreader.core.datasource.spec.user.UserStorageSpecification;
import com.worldreader.core.domain.interactors.user.application.IsAnonymousUserInteractor;
import com.worldreader.core.domain.model.user.User2;
import com.worldreader.core.domain.repository.UserRepository;

import javax.inject.Inject;
import java.util.concurrent.*;

@PerActivity public class SendLocalLibraryInteractor {

  private final IsAnonymousUserInteractor isAnonymousUserInteractor;
  private final SaveUserInteractor saveUserInteractor;
  private final GetUserInteractor getUserInteractor;

  private final ListeningExecutorService executor;
  private final UserRepository repository;

  @Inject public SendLocalLibraryInteractor(final IsAnonymousUserInteractor isAnonymousUserInteractor, final SaveUserInteractor saveUserInteractor,
      final GetUserInteractor getUserInteractor, final ListeningExecutorService executor, final UserRepository repository) {

    this.isAnonymousUserInteractor = isAnonymousUserInteractor;
    this.saveUserInteractor = saveUserInteractor;
    this.getUserInteractor = getUserInteractor;
    this.executor = executor;
    this.repository = repository;
  }

  public ListenableFuture<Boolean> execute(final String localLibrary) {
    return executor.submit(getInteractorCallable(localLibrary));
  }

  private Callable<Boolean> getInteractorCallable(final String localLibrary) {
    return new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        final IsAnonymousUserInteractor.Type type = isAnonymousUserInteractor.execute(MoreExecutors.directExecutor()).get();

        switch (type) {
          case ANONYMOUS:
          case NONE:
            return handleAnonymousUser(localLibrary);
          case REGISTERED:
            return handleRegisteredUser(localLibrary, type);
        }

        return false;
      }
    };
  }

  private Boolean handleAnonymousUser(final String localLibrary) throws ExecutionException, InterruptedException {
    // TODO: 28/06/2017 Flavia put Amazon SDK parameters here to be sent
    // Send here whatever is needed before continuing
    // End

    // Update anonymous user local library field
    final User2 user =
        getUserInteractor.execute(new UserStorageSpecification(UserStorageSpecification.UserTarget.FIRST_LOGGED_IN_FALLBACK_TO_ANONYMOUS),
            MoreExecutors.directExecutor()).get();
    final User2 updatedUser = new User2.Builder(user).setLocalLibrary(localLibrary).build();
    saveUserInteractor.execute(updatedUser, SaveUserInteractor.Type.ANONYMOUS, MoreExecutors.directExecutor()).get();

    return true;
  }

  private boolean handleRegisteredUser(final String localLibrary, final IsAnonymousUserInteractor.Type type) {
    // As this call doesn't involve threading we can safely that the result will be returned prior to returning
    repository.sendLocalLibrary(localLibrary, new Callback<Boolean>() {
      @Override public void onSuccess(final Boolean b) {
        final User2 user;
        try {
          user = getUserInteractor.execute(MoreExecutors.directExecutor()).get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }

        final User2 updatedUser = new User2.Builder(user).setLocalLibrary(localLibrary).build();

        saveUserInteractor.execute(updatedUser,
            type == IsAnonymousUserInteractor.Type.ANONYMOUS ? SaveUserInteractor.Type.ANONYMOUS : SaveUserInteractor.Type.LOGGED_IN);
      }

      @Override public void onError(final Throwable e) {
        throw new RuntimeException(e);
      }
    });

    return true;
  }

}