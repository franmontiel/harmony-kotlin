package com.worldreader.core.datasource.network.general.retrofit.interceptor;

import android.support.annotation.NonNull;
import com.worldreader.core.application.di.qualifiers.WorldreaderBookApiEndpointToken;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of the okHttp interceptor to comply with the requirements of the WorldReader
 * Server.
 */
public class WorldreaderBooksServerInterceptor implements Interceptor {

  public static final String TAG = "SERVER_WORLDREADER";

  private static final String WORLDREADER_ANDROID_CLIENT = "org.worldreader.wrms.android/1.0.0.0";

  private final String token;

  @Inject public WorldreaderBooksServerInterceptor(final @WorldreaderBookApiEndpointToken String token) {
    this.token = token;
  }

  @Override public Response intercept(@NonNull Chain chain) throws IOException {
    Request request = chain.request();

    //Build new request
    try {
      String timestamp = String.valueOf(System.currentTimeMillis());
      String hash = buildRequestHash(request.url(), timestamp);

      request = request.newBuilder()
          .url(request.url())
          .addHeader("X-Worldreader-Client", WORLDREADER_ANDROID_CLIENT)
          .addHeader("X-Worldreader-Timestamp", timestamp)
          .addHeader("X-Worldreader-Hash", hash)
          .build();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return chain.proceed(request);
  }

  /**
   * Generate the full request path
   *
   * @param httpUrl - {@link HttpUrl} Object to get all the parts of the request
   *
   * @return Full path request
   */
  @NonNull private String buildPath(HttpUrl httpUrl) {
    StringBuilder encodedPathBuilder = new StringBuilder();

    encodedPathBuilder.append(httpUrl.encodedPath());

    String query = httpUrl.encodedQuery();
    if (query != null && query.length() > 0) {
      encodedPathBuilder.append("?");
      encodedPathBuilder.append(query);
    }

    return encodedPathBuilder.toString();
  }

  /**
   * Generate the String that we need to generate the hash for the request header.
   * Example of value to generate: (path + timestamp + token)
   *
   * @param httpUrl - {@link HttpUrl} Object to get all the parts of the request
   * @param timestamp - It's a requirement of the API
   *
   * @return Value to make the hash
   */
  @NonNull private String buildStringToHash(HttpUrl httpUrl, String timestamp) {
    final String path = buildPath(httpUrl);
    return path + timestamp + token;
  }

  /**
   * Generate the hash that we need to put on the header for the request authentication.
   *
   * @param httpUrl - {@link HttpUrl} Object to get all the parts of the request
   * @param timestamp - It's a requirement of the API
   *
   * @return Request hash
   *
   * @throws NoSuchAlgorithmException
   */
  private String buildRequestHash(HttpUrl httpUrl, String timestamp) throws NoSuchAlgorithmException {
    final String valueToHash = buildStringToHash(httpUrl, timestamp);
    return sha256(valueToHash);
  }

  private String sha256(String value) throws NoSuchAlgorithmException {
    final MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(value.getBytes());

    byte byteData[] = md.digest();

    //convert the byte to hex format method 1
    final StringBuilder sb = new StringBuilder();
    for (byte aByteData : byteData) {
      sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
  }
}