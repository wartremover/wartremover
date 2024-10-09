package org.wartremover

trait PositionCompat { self: Position =>
  final def asTupleOption = Option(Tuple.fromProductTyped(this))
}
