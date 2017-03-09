package com.worldreader.core.datasource.network.retrofit2.manager;

import com.google.gson.Gson;
import com.worldreader.core.datasource.network.general.retrofit.AbstractRetrofitApiManager;
import com.worldreader.core.datasource.network.retrofit2.services.AuthApiService2;
import com.worldreader.core.datasource.network.retrofit2.services.OAuthApiService2;
import com.worldreader.core.datasource.network.retrofit2.services.UserApiService2;
import com.worldreader.core.datasource.network.retrofit2.services.UserBooksApiService;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.*;

public class WorldreaderUserRetrofitApiManager2 extends AbstractRetrofitApiManager {

  private final HttpUrl endpoint;
  private final OkHttpClient okHttpClient;
  private final Executor callbackExecutor;
  private final Gson gson;

  public WorldreaderUserRetrofitApiManager2(HttpUrl endpoint, OkHttpClient okHttpClient,
      Executor callbackExecutor, Gson gson) {
    this.okHttpClient = okHttpClient;
    this.endpoint = endpoint;
    this.callbackExecutor = callbackExecutor;
    this.gson = gson;
  }

  @Override protected Retrofit createRetrofit() {
    return new Builder(endpoint).setOkHttpClient(okHttpClient)
        .setCallbackExecutor(callbackExecutor)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .create();
  }

  public AuthApiService2 authApiService() {
    return createService(AuthApiService2.class);
  }

  public OAuthApiService2 oAuthApiService() {
    return createService(OAuthApiService2.class);
  }

  public UserApiService2 userApiService() {
    return createService(UserApiService2.class);
  }

  public UserBooksApiService userBooksApiService() {
    return createService(UserBooksApiService.class);
  }
}
