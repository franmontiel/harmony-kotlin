package com.worldreader.core.domain.model;

import android.support.annotation.IntDef;
import com.worldreader.core.datasource.model.ContentOpfEntity;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

public class BookMetadata implements Serializable {

  public static final int FILE_MODE = 0;
  public static final int STREAMING_MODE = 1;

  public static final String BOOK_FILE_EXTRA = "book.file.extra";

  // Mandatory params
  @BookMetadataMode public int mode;
  public String bookId;
  public String version;
  public String title;
  public String author;
  public int collectionId;
  public Map<String, Object> extras = new HashMap<>();

  // Optional (Only used in streaming mode and will be deprecated in near future favoring the usage of a Map)
  public String contentOpfName;
  public String tocResourceName;
  public List<String> resources;
  public Map<String, ContentOpfEntity.Item> imagesResources;

  @IntDef({
      FILE_MODE, STREAMING_MODE
  }) @Retention(RetentionPolicy.SOURCE) @interface BookMetadataMode {

  }
}
