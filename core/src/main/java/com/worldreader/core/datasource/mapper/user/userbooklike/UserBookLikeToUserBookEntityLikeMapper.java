package com.worldreader.core.datasource.mapper.user.userbooklike;

import com.google.common.base.Optional;
import com.worldreader.core.datasource.mapper.Mapper;
import com.worldreader.core.datasource.model.user.userbooklikes.UserBookLikeEntity;
import com.worldreader.core.domain.model.user.UserBookLike;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class UserBookLikeToUserBookEntityLikeMapper implements Mapper<Optional<UserBookLike>, Optional<UserBookLikeEntity>> {

  @Inject public UserBookLikeToUserBookEntityLikeMapper() {
  }

  @Override public Optional<UserBookLikeEntity> transform(final Optional<UserBookLike> optional) {
    if (!optional.isPresent()) {
      return Optional.absent();
    } else {
      final UserBookLike raw = optional.get();
      final UserBookLikeEntity entity = new UserBookLikeEntity.Builder()
          .withBookId(raw.getBookId())
          .withUserId(raw.getUserId())
          .withLiked(raw.isLiked())
          .withSync(raw.isSync())
          .withLikedAt(raw.getLikedAt())
          .build();
      return Optional.of(entity);
    }
  }

}