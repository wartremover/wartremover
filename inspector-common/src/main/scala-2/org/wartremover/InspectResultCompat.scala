package org.wartremover

trait InspectResultCompat { self: InspectResult =>
  final def asTupleOption = InspectResult.unapply(this)
}
