package com.worldreader.core.datasource.spec.score;

import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.worldreader.core.datasource.storage.datasource.cache.manager.table.UserScoreTable;

public class DeleteAllUserScoreSpec extends UserScoreStorageSpecification {

  @Override public Query toQuery() {
    return null;
  }

  @Override public DeleteQuery toDeleteQuery() {
    return DeleteQuery.builder().table(UserScoreTable.TABLE).build();
  }

}