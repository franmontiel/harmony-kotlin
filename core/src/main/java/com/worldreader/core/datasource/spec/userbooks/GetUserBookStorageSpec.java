package com.worldreader.core.datasource.spec.userbooks;

import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.worldreader.core.datasource.storage.datasource.cache.manager.table.UserBooksTable;

public class GetUserBookStorageSpec extends UserBookStorageSpecification {

  public GetUserBookStorageSpec(final String bookId, final String userId) {
    super(bookId, userId);
  }

  @Override public Query toQuery() {
    return Query.builder()
        .table(UserBooksTable.TABLE)
        .where(UserBooksTable.COLUMN_BOOK_ID
            + " LIKE ? AND "
            + UserBooksTable.COLUMN_USER_ID
            + " LIKE ?")
        .whereArgs(getBookId(), getUserId())
        .build();
  }

  @Override public DeleteQuery toDeleteQuery() {
    return null;
  }
}
