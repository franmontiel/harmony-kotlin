@file:Suppress("IllegalIdentifier")

package com.harmony.kotlin.data.datasource.memory

import arrow.atomic.AtomicInt
import com.harmony.kotlin.common.BaseTest
import com.harmony.kotlin.common.randomString
import com.harmony.kotlin.data.query.KeyQuery
import com.harmony.kotlin.data.query.VoidQuery
import com.harmony.kotlin.error.DataNotFoundException
import com.harmony.kotlin.error.QueryNotSupportedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class InMemoryDataSourceTests : BaseTest() {

  @Test
  fun `should throw DataNotFoundException if value is missing`() = runTest {
    assertFailsWith<DataNotFoundException> {
      val inMemoryDataSource = givenInMemoryDataSource<String>()
      val query = KeyQuery(randomString())

      inMemoryDataSource.get(query)
    }
  }

  @Test
  fun `should throw QueryNotSupportedException if query is invalid when get function is called`() = runTest {
    assertFailsWith<QueryNotSupportedException> {
      val inMemoryDataSource = givenInMemoryDataSource<String>()
      val invalidQuery = VoidQuery

      inMemoryDataSource.get(invalidQuery)
    }
  }

  @Test
  fun `should response the value if exist`() = runTest {
    val pair = Pair(KeyQuery(randomString()), randomString())
    val inMemoryDataSource = givenInMemoryDataSource(insertValue = pair)

    val response = inMemoryDataSource.get(pair.first)

    assertEquals(pair.second, response)
  }

  @Test
  fun `should response the values if exist using get`() = runTest {
    val pair = Pair(KeyQuery(randomString()), listOf(randomString(), randomString()))
    val inMemoryDataSource = givenInMemoryDataSourceOfList(insertValues = pair)

    val response = inMemoryDataSource.get(pair.first)

    assertEquals(pair.second, response)
  }

  @Test
  fun `should throw QueryNotSupportedException if query is invalid when put function is called`() = runTest {
    assertFailsWith<QueryNotSupportedException> {
      val inMemoryDataSource = givenInMemoryDataSource<String>()
      val invalidQuery = VoidQuery

      inMemoryDataSource.put(invalidQuery, randomString())
    }
  }

  @Test
  fun `should throw IllegalArgumentException if the value is null when put function is called`() = runTest {
    assertFailsWith<IllegalArgumentException> {
      val inMemoryDataSource = givenInMemoryDataSource<String>()
      val query = KeyQuery(randomString())

      inMemoryDataSource.put(query, null)
    }
  }

  @Test
  fun `should store value if query is valid when put function is called`() = runTest {
    val inMemoryDataSource = givenInMemoryDataSource<String>()
    val query = KeyQuery(randomString())
    val expectedValue = randomString()

    inMemoryDataSource.put(query, expectedValue)
    val response = inMemoryDataSource.get(query)

    assertEquals(expectedValue, response)
  }

  @Test
  fun `should throw QueryNotSupportedException if query is invalid when delete function is called`() = runTest {
    assertFailsWith<QueryNotSupportedException> {
      val inMemoryDataSource = givenInMemoryDataSource<String>()
      val invalidQuery = VoidQuery

      inMemoryDataSource.delete(query = invalidQuery)
    }
  }

  @Test
  fun `should delete value if exist`() = runTest {
    val valueToInsert = Pair(KeyQuery(randomString()), randomString())

    val inMemoryDataSource = givenInMemoryDataSource(insertValue = valueToInsert)

    inMemoryDataSource.delete(query = valueToInsert.first)

    assertFailsWith<DataNotFoundException> {
      inMemoryDataSource.get(valueToInsert.first)
    }
  }

  @Test
  fun `should not fail because InvalidMutabilityException `() = runTest {
    class OutsideScope {
      val inMemoryDataSource = InMemoryDataSource<String>()
    }

    val scope = OutsideScope()
    val pair = Pair(KeyQuery(randomString()), randomString())
    scope.inMemoryDataSource.put(pair.first, pair.second)
  }

  @Test
  @Suppress("SwallowedException")
  fun `should not have concurrency problems`() = kotlinx.coroutines.test.runTest {
    val key = AtomicInt(0)
    val inMemoryDataSource = givenInMemoryDataSource<Int>(Pair(KeyQuery(key.get().toString()), 0))

    val numThreads = 10
    val numIterationsPerThread = 1000

    val jobs = List(numThreads) {
      launch(Dispatchers.IO) {
        for (i in 0 until numIterationsPerThread) {
          try {
            inMemoryDataSource.put(KeyQuery(key.incrementAndGet().toString()), 0)
          } catch (e: Exception) {
            fail("Concurrency problem: putting element ${key.get()} failed with ${e.stackTraceToString()}")
          }
        }
      }
    }

    jobs.joinAll()

    // Ensure that the final state of the hashmap is consistent with expectations
    val numberOfElements = numThreads * numIterationsPerThread
    for (currentKey in 0..numberOfElements) {
      try {
        inMemoryDataSource.get(KeyQuery(currentKey.toString()))
      } catch (e: DataNotFoundException) {
        fail("Concurrency problem: element $currentKey not found because it was not inserted in the first place")
      }
    }
  }

  private suspend fun <T> givenInMemoryDataSource(
    insertValue: Pair<KeyQuery, T>? = null,
  ): InMemoryDataSource<T> {
    val inMemoryDataSource = InMemoryDataSource<T>()

    insertValue?.let {
      inMemoryDataSource.put(it.first, it.second)
    }

    return inMemoryDataSource
  }

  private suspend fun givenInMemoryDataSourceOfList(
    insertValues: Pair<KeyQuery, List<String>>? = null,
  ): InMemoryDataSource<List<String>> {
    val inMemoryDataSource = InMemoryDataSource<List<String>>()

    insertValues?.let {
      inMemoryDataSource.put(it.first, it.second)
    }

    return inMemoryDataSource
  }
}
