package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class ExistentialTest extends FunSuite {
  test("can use existential values") {
    val result = WartTestTraverser(Unsafe) {
      case class Name[A](value: String)
      def values(names: Name[_]*) =
        names map { n => n.value }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
