package com.worldreader.core.datasource.network.datasource.banner;

import com.worldreader.core.common.deprecated.callback.CompletionCallback;
import com.worldreader.core.datasource.model.BannerEntity;

import java.util.*;

public interface BannerNetworkDataSource {

  void fetchMainBanners(int index, int limit, CompletionCallback<List<BannerEntity>> callback);

  void fetchCollectionBanners(int index, int limit,
      CompletionCallback<List<BannerEntity>> callback);
}
