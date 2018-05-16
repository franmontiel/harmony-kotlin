package com.worldreader.core.datasource.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class OAuthNetworkResponseEntity {

  @SerializedName("access_token") private String accessToken;
  @SerializedName("expires_in") private long expiresIn;
  @SerializedName("token_type") private String tokenType;
  @SerializedName("scope") private String scope;
  @SerializedName("refresh_token") private String refreshToken;

  private final Date creationDate;

  public OAuthNetworkResponseEntity() {
    this.creationDate = new Date();
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  @Override public String toString() {
    return "OAuthNetworkResponseEntity{" +
        "accessToken='" + accessToken + '\'' +
        ", expiresIn=" + expiresIn +
        ", tokenType='" + tokenType + '\'' +
        ", scope='" + scope + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        ", creationDate=" + creationDate +
        '}';
  }
}
