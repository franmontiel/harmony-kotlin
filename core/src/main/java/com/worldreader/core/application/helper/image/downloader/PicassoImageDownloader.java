package com.worldreader.core.application.helper.image.downloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.mobilejazz.logger.library.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.sromku.simple.storage.Storage;
import com.worldreader.core.application.helper.image.ImageDownloader;
import com.worldreader.core.application.helper.image.ImageLoader;

import javax.inject.Inject;
import java.io.*;

public class PicassoImageDownloader implements ImageDownloader {

  private static final String IMAGE_CACHE_FOLDER = "image-cache";
  private static final String TAG = ImageLoader.class.getSimpleName();

  private final Picasso picasso;
  private final Storage storage;
  private final Logger logger;

  @Inject public PicassoImageDownloader(Picasso picasso, Storage storage, Logger logger) {
    this.picasso = picasso;
    this.storage = storage;
    this.logger = logger;

  }

  @Override public void download(final String key, final String url) {
    picasso.load(url).into(new Target() {
      @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        logger.d(TAG, "Imaged loaded" + " {url: " + url + ", from: " + from.toString() + "}");

        boolean directoryExists = storage.isDirectoryExists(IMAGE_CACHE_FOLDER);
        if (directoryExists) {
          storage.createFile(IMAGE_CACHE_FOLDER, getImageFileName(key), bitmap);
        } else {
          storage.createDirectory(IMAGE_CACHE_FOLDER, false);
        }
      }

      @Override public void onBitmapFailed(Drawable errorDrawable) {
        logger.e(TAG, "Error downloading the image with url: " + url);
      }

      @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Nothing to do
      }
    });
  }

  @Override public boolean delete(String key) {
    String imageFileName = getImageFileName(key);
    return storage.deleteFile(IMAGE_CACHE_FOLDER, imageFileName);
  }

  @Override public File getImage(String key) {
    return storage.getFile(IMAGE_CACHE_FOLDER, getImageFileName(key));
  }

  //region Private methods
  private String getImageFileName(String key) {
    return key + ".jpg";
  }
  //endregion
}