package com.worldreader.core.domain.model;

import java.io.*;
import java.util.*;

public class BookMetadata implements Serializable {

  public static final BookMetadata EMPTY = new BookMetadata();

  private String bookId;
  private int collectionId;
  private String relativeContentUrl;
  private String contentOpfName;
  private String tocResourceName;
  private List<String> resources;
  private boolean streaming;

  // Extra fields for BookFinished
  private String title;
  private String author;

  public String getBookId() {
    return bookId;
  }

  public void setBookId(String bookId) {
    this.bookId = bookId;
  }

  public boolean isStreaming() {
    return streaming;
  }

  public void setStreaming(boolean streaming) {
    this.streaming = streaming;
  }

  public String getContentOpfName() {
    return contentOpfName;
  }

  public void setContentOpfName(String contentOpfName) {
    this.contentOpfName = contentOpfName;
  }

  public void setRelativeContentUrl(String relativeUrl) {
    this.relativeContentUrl = relativeUrl;
  }

  public String getRelativeContentUrl() {
    return this.relativeContentUrl;
  }

  public String getTocResource() {
    return tocResourceName;
  }

  public void setTocResource(String tocResource) {
    this.tocResourceName = tocResource;
  }

  public int getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(int collectionId) {
    this.collectionId = collectionId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public List<String> getResources() {
    return resources;
  }

  public void setResources(List<String> resources) {
    this.resources = resources;
  }

  public boolean isImage(String resource) {
    return resource.contains("png") || resource.contains("jpg") || resource.contains("jpeg");
  }

  public String addStaticImageSize(String resource) {
    return resource + "?size=480x800";
  }
}
