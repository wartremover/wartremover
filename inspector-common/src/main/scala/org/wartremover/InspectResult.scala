package org.wartremover

case class InspectResult(
  errors: List[Diagnostic],
  warnings: List[Diagnostic],
  stderr: String,
)

object InspectResult {
  val empty: InspectResult = InspectResult(Nil, Nil, "")
}
