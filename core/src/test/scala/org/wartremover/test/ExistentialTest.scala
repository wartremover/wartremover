package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.Unsafe

class ExistentialTest extends AnyFunSuite with ResultAssertions {
  test("can use existential values") {
    val result = WartTestTraverser(Unsafe) {
      case class Name[A](value: String)
      def values(names: Name[?]*) =
        names map { n => n.value }
    }
    assertEmpty(result)
  }
}
