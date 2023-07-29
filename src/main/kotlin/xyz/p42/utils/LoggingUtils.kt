package xyz.p42.utils

import java.util.Date
import java.util.logging.ConsoleHandler
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class LoggingUtils {
  companion object {
    var logger: Logger

    init {
      val mainLogger = Logger.getLogger("xyz.p42")
      mainLogger.useParentHandlers = false
      val handler = ConsoleHandler()
      handler.formatter = object : SimpleFormatter() {
        private val format = "[%1\$tF %1\$tT] [%2$-7s] %3\$s %n"

        @Synchronized
        override fun format(lr: LogRecord): String {
          return java.lang.String.format(
            format,
            Date(lr.millis),
            lr.level.localizedName,
            lr.message
          )
        }
      }
      mainLogger.addHandler(handler)
      logger = mainLogger
    }
  }
}
