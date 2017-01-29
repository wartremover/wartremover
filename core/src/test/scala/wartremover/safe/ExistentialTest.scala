package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class ExistentialTest extends FunSuite with ResultAssertions {
  test("can use existential values") {
    val result = WartTestTraverser(Unsafe) {
      case class Name[A](value: String)
      def values(names: Name[_]*) =
        names map { n => n.value }
    }
    assertEmpty(result)
  }
}
