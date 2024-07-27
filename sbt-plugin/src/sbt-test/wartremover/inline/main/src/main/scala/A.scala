package example

import play.api.libs.json.Json

case class A(x: Double)

object A {
  val w = Json.writes[A]
}
