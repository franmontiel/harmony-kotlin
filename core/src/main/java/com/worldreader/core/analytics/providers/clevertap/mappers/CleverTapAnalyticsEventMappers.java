package com.worldreader.core.analytics.providers.clevertap.mappers;

import android.content.Context;
import com.worldreader.core.analytics.event.AnalyticsEvent;
import com.worldreader.core.analytics.event.books.BookContinueReadingAnalyticsEvent;
import com.worldreader.core.analytics.event.books.BookDetailAnalyticsEvent;
import com.worldreader.core.analytics.event.books.BookFinishedAnalyticsEvent;
import com.worldreader.core.analytics.event.books.BookOpenAnalyticsEvent;
import com.worldreader.core.analytics.event.books.BookReadAnalyticsEvent;
import com.worldreader.core.analytics.event.categories.CategoryBooksAnalyticsEvent;
import com.worldreader.core.analytics.event.categories.CategorySelectedAnalyticsEvent;
import com.worldreader.core.analytics.event.other.MoreBooksAnalyticsEvent;
import com.worldreader.core.analytics.event.register.ProfileAnalyticsEvent;
import com.worldreader.core.analytics.event.register.SignInAnalyticsEvent;
import com.worldreader.core.analytics.event.register.SignUpAnalyticsEvent;
import com.worldreader.core.analytics.mapper.AnalyticsEventMappers;

import java.util.*;

public class CleverTapAnalyticsEventMappers implements AnalyticsEventMappers<CleverTapAnalyticsMapper<? extends AnalyticsEvent>> {

  public static final CleverTapAnalyticsMapper<AnalyticsEvent> NONE = new CleverTapAnalyticsMapper<AnalyticsEvent>() {
    @Override public Map<String, Object> transform(AnalyticsEvent event) {
      return Collections.emptyMap();
    }
  };

  private final Map<Class<?>, CleverTapAnalyticsMapper<? extends AnalyticsEvent>> mappers;

  public CleverTapAnalyticsEventMappers(Context context) {
    this.mappers = createMappers(context);
  }

  private Map<Class<?>, CleverTapAnalyticsMapper<? extends AnalyticsEvent>> createMappers(final Context context) {
    final Map<Class<?>, CleverTapAnalyticsMapper<? extends AnalyticsEvent>> m = new HashMap<Class<?>, CleverTapAnalyticsMapper<? extends AnalyticsEvent>>() {{

      put(CategorySelectedAnalyticsEvent.class, new CleverTapCategoryDetailsMapper(context));
      put(CategoryBooksAnalyticsEvent.class, new CleverTapCategoryBooksMapper(context));
      put(BookReadAnalyticsEvent.class, new CleverTapBookReadMapper(context));
      put(BookContinueReadingAnalyticsEvent.class, new CleverTapBookContinueReadingMapper(context));
      put(BookOpenAnalyticsEvent.class, new CleverTapBookOpenMapper(context));
      put(BookFinishedAnalyticsEvent.class, new CleverTapBookFinishedMapper(context));
      put(BookDetailAnalyticsEvent.class, new CleverTapBookDetailsMapper(context));
      put(SignUpAnalyticsEvent.class, new CleverTapSignUpMapper(context));
      put(SignInAnalyticsEvent.class, new CleverTapSignInMapper(context));
      put(ProfileAnalyticsEvent.class, new CleverTapProfileMapper(context));
      put(MoreBooksAnalyticsEvent.class, new CleverTapMoreBooksMapper(context));

      //CategoryBooks
    }};
    return Collections.unmodifiableMap(m);
  }

  @Override public CleverTapAnalyticsMapper<? extends AnalyticsEvent> obtain(Class<?> clazz) {
    return mappers.get(clazz);
  }
}

/**

 * BookContinue
 * BookDetail
 * BookRead
 * BookStart
 * SingIn
 * SingUp
 * BookEnd
 * CategoryDetails
 * CategoryBooks
 *
 * The web client is sending the PageView event. We may want to do a ScreenView equivalent one...but I'm not sure about the use of that info in CT
 */