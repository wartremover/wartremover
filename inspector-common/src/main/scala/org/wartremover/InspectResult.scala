package org.wartremover

case class InspectResult(
  errors: List[Diagnostic],
  warnings: List[Diagnostic],
  stderr: String,
) extends InspectResultCompat

object InspectResult {
  val empty: InspectResult = InspectResult(Nil, Nil, "")
}
