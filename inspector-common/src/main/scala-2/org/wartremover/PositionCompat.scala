package org.wartremover

trait PositionCompat { self: Position =>
  final def asTupleOption = Position.unapply(this)
}
