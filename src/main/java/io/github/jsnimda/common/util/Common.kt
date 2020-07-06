package io.github.jsnimda.common.util


// ============
// tryCatch()
// ============

// use this functions if the exceptions is unusual and not expected (typically IOException)

inline fun <T> trySwallow(tryToRun: () -> T): T? = trySwallow(null, tryToRun)
inline fun <T> trySwallow(failureValue: T, tryToRun: () -> T): T = tryCatch({ failureValue }, tryToRun)

inline fun <R> tryOrNull(onFailure: (Throwable) -> Unit, tryToRun: () -> R): R? =
  tryCatch({ onFailure(it); null }, tryToRun)

inline fun <T> tryCatch(tryToRun: () -> T): T? = tryCatch(null, tryToRun)
inline fun <T> tryCatch(failureValue: T, tryToRun: () -> T): T =
  tryCatch({ it.printStackTrace(); failureValue }, tryToRun)

inline fun <R> tryCatch(onFailure: (Throwable) -> R, tryToRun: () -> R): R =
  try {
    tryToRun()
  } catch (e: Throwable) {
    onFailure(e)
  }

// ============
// Event
// ============

class Event<T> {
  private val handlers = mutableSetOf<((data: T) -> Unit)>()
  operator fun plusAssign(handler: (T) -> Unit) {
    handlers.add(handler)
  }

  operator fun minusAssign(handler: (T) -> Unit) {
    handlers.remove(handler)
  }

  operator fun invoke(data: T) {
    handlers.forEach { it(data) }
  }
}

class RoutedEvent<T> {
  private val handlers = mutableSetOf<((data: T, handled: Boolean) -> Boolean)>()
  operator fun plusAssign(handler: (T, handled: Boolean) -> Boolean) {
    handlers.add(handler)
  }

  operator fun minusAssign(handler: (T, handled: Boolean) -> Boolean) {
    handlers.remove(handler)
  }

  operator fun invoke(data: T, handled: Boolean): Boolean {
    return handlers.map { it(data, handled) }.any { it }
  }
}
