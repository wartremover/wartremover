package org.wartremover

trait DiagnosticCompat { self: Diagnostic =>
  final def asTupleOption = Diagnostic.unapply(this)
}
