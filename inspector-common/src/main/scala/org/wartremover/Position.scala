package org.wartremover

final case class Position(
  start: Int,
  startLine: Int,
  startColumn: Int,
  end: Int,
  endLine: Int,
  endColumn: Int,
  path: String,
  sourceCode: Option[String],
)
