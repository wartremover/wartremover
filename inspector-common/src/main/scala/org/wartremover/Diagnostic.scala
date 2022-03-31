package org.wartremover

final case class Diagnostic(
  message: String,
  wart: String,
  position: Position
)
