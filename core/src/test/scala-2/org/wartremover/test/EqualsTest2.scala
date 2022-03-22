package org.wartremover
package test

import org.wartremover.warts.Equals
import org.scalatest.funsuite.AnyFunSuite

class EqualsTest2 extends AnyFunSuite with ResultAssertions {
  test("can't use == or != method on primitives") {
    val result1 = WartTestTraverser(Equals) {
      5 == "foo"
    }
    assertError(result1)("== is disabled - use === or equivalent instead")

    val result2 = WartTestTraverser(Equals) {
      5 != "foo"
    }
    assertError(result2)("!= is disabled - use =/= or equivalent instead")
  }

  test("can't use == or != on case classes") {
    case class Foo(i: Int)
    val result3 = WartTestTraverser(Equals) {
      "abc" == Foo(4)
    }
    assertError(result3)("== is disabled - use === or equivalent instead")
  }
}
