package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

class ExistentialTest extends AnyFunSuite with ResultAssertions {
  test("can use existential values") {
    val result = WartTestTraverser(Unsafe) {
      case class Name[A](value: String)
      def values(names: Name[_]*) =
        names map { n => n.value }
    }
    assertEmpty(result)
  }
}
