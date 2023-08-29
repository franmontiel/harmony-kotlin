package com.mobilejazz.kmmsample.core.feature.hackerposts.domain.interactor

import arrow.core.Either
import arrow.core.raise.either
import com.harmony.kotlin.domain.interactor.either.GetInteractor
import com.harmony.kotlin.error.HarmonyException
import com.mobilejazz.kmmsample.core.feature.hackerposts.domain.HackerNewsQuery
import com.mobilejazz.kmmsample.core.feature.hackerposts.domain.model.HackerNewsPost
import com.mobilejazz.kmmsample.core.feature.hackerposts.domain.model.HackerNewsPosts
import com.mobilejazz.kmmsample.core.feature.hackerposts.domain.model.HackerNewsPostsIds
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class GetHackerNewsPostsInteractor(
  private val coroutineContext: CoroutineContext,
  private val getHackerNewsIdsPostsInteractor: GetInteractor<HackerNewsPostsIds>,
  private val getHackerNewsPostInteractor: GetInteractor<HackerNewsPost>,
) {
  suspend operator fun invoke(): Either<HarmonyException, HackerNewsPosts> {
    return withContext(coroutineContext) {
      either {
        getHackerNewsIdsPostsInteractor<HarmonyException>(HackerNewsQuery.GetAll).bind()
          .listIds
          .take(5)
          .map { postId ->
            getHackerNewsPostInteractor<HarmonyException>(HackerNewsQuery.GetPost(postId)).bind()
          }
      }
    }
  }
}
