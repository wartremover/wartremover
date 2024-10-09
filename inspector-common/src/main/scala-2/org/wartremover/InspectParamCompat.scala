package org.wartremover

trait InspectParamCompat { self: InspectParam =>
  final def asTupleOption = InspectParam.unapply(this)
}
