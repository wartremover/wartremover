package org.wartremover

trait InspectResultCompat { self: InspectResult =>
  final def asTupleOption = Option(Tuple.fromProductTyped(this))
}
