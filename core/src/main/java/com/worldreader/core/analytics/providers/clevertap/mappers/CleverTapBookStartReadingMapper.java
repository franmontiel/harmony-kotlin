package com.worldreader.core.analytics.providers.clevertap.mappers;

import com.worldreader.core.analytics.event.books.BookStartReadingAnalyticsEvent;
import java.util.HashMap;
import java.util.Map;

public class CleverTapBookStartReadingMapper implements CleverTapAnalyticsMapper<BookStartReadingAnalyticsEvent> {

  @Override public Map<String, Object> transform(final BookStartReadingAnalyticsEvent event) {
    return new HashMap<String, Object>() {{
      put("BookId", event.getId());
      put("BookTitle", event.getTitle());
      put("BookVersion", event.getVersion());
      put("BookPublisher", event.getPublisher());
      put("BookCategory", event.getCategory());
      put("BookCategoryId", event.getCategoryId());
      put("Os", "Android");
      put("Country", "");
      put("UserId", "");
    }};
  }
}
