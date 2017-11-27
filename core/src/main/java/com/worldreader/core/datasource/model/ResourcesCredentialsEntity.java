package com.worldreader.core.datasource.model;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class ResourcesCredentialsEntity {

  @SerializedName("host") private String host;
  @SerializedName("prefix") private String prefix;
  @SerializedName("query_string") private String query;
  @SerializedName("valid_until") private Date date;

  public ResourcesCredentialsEntity() {
  }

  public String getHost() {
    return host;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getQuery() {
    return query;
  }

  public Date getDate() {
    return date;
  }

  @Override public String toString() {
    return "ResourcesCredentialsEntity{"
        + "host='"
        + host
        + '\''
        + ", prefix='"
        + prefix
        + '\''
        + ", query='"
        + query
        + '\''
        + ", date="
        + date
        + '}';
  }
}