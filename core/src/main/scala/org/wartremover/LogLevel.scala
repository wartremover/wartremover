package org.wartremover

sealed abstract class LogLevel(private val value: String) extends Product with Serializable

object LogLevel {
  case object Debug extends LogLevel("debug")
  case object Info extends LogLevel("info")
  case object Disable extends LogLevel("disable")

  private[this] val values: List[LogLevel] = Debug :: Info :: Disable :: Nil

  val map: Map[String, LogLevel] = values.map(x => x.value -> x).toMap
}
