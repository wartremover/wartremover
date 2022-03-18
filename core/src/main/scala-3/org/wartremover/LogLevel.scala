package org.wartremover

enum LogLevel(private val value: String) {
  case Disable extends LogLevel("disable")
  case Info extends LogLevel("info")
  case Debug extends LogLevel("debug")
}
object LogLevel {
  val map: Map[String, LogLevel] = this.values.map(x => x.value -> x).toMap
}
