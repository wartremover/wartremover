package org.wartremover

trait InspectParamCompat { self: InspectParam =>
  final def asTupleOption = Option(Tuple.fromProductTyped(this))
}
