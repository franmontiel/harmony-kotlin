package com.harmony.kotlin.data.validator.vastra.strategy

import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategy
import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategyDataSource
import com.harmony.kotlin.data.validator.vastra.strategies.ValidationStrategyResult
import java.util.*

abstract class TimestampValidationStrategyDataSource(open var lastUpdate: Date = Date(), open var expiryTime: Long) : ValidationStrategyDataSource

class TimestampValidationStrategy : ValidationStrategy {

  override fun <T : ValidationStrategyDataSource> isValid(t: T): ValidationStrategyResult {
    when (t) {
      is TimestampValidationStrategyDataSource -> {
        val diff = System.currentTimeMillis() - t.lastUpdate.time

        return if (diff > t.expiryTime)
          ValidationStrategyResult.INVALID
        else
          ValidationStrategyResult.VALID
      }
      else -> {
        throw IllegalArgumentException("object != TimestampValidationStrategyDataSource")
      }
    }
  }
}