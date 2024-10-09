package org.wartremover

trait DiagnosticCompat { self: Diagnostic =>
  final def asTupleOption = Option(Tuple.fromProductTyped(this))
}
