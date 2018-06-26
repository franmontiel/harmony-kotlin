package com.worldreader.core.application.helper.image;

import android.content.Context;
import android.widget.ImageView;

public interface ImageLoader {

  void load(String id, String url, ImageView imageView);

  void load(String id, String url, int resPlaceholderIcon, ImageView imageView);

  void loadImageCircle(String url, int resPlaceholderIcon, ImageView imageView);

  void load(int resIcon, ImageView imageView);

  void loadCover(String url, String tag, int radius, int margin, ImageView view);

  void cancel(String tag);

  Context getContext();
}
