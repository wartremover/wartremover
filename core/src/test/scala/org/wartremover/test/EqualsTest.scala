package org.wartremover
package test

import org.wartremover.warts.Equals
import org.scalatest.funsuite.AnyFunSuite

class EqualsTest extends AnyFunSuite with ResultAssertions {
  test("can't use == or != on classes") {
    class Foo(i: Int)

    val result1 = WartTestTraverser(Equals) {
      new Foo(5) == new Foo(4)
    }
    assertError(result1)("== is disabled - use === or equivalent instead")

    val result2 = WartTestTraverser(Equals) {
      new Foo(5) != new Foo(4)
    }
    assertError(result2)("!= is disabled - use =/= or equivalent instead")
  }

  test("can't use == or != on case classes") {
    case class Foo(i: Int)

    val result1 = WartTestTraverser(Equals) {
      Foo(5) == Foo(4)
    }
    assertError(result1)("== is disabled - use === or equivalent instead")

    val result2 = WartTestTraverser(Equals) {
      Foo(5) != Foo(4)
    }
    assertError(result2)("!= is disabled - use =/= or equivalent instead")
  }

  test("can't use equals") {
    class Foo(i: Int)

    val result = WartTestTraverser(Equals) {
      new Foo(5).equals(new Foo(4))

      5.equals("foo")
    }
    assertErrors(result)("equals is disabled - use === or equivalent instead", 2)
  }

  test("can't use overridden equals") {
    class Foo(i: Int) {
      override def equals(obj: scala.Any) = false
    }

    val result = WartTestTraverser(Equals) {
      new Foo(1).equals(1)
    }
    assertError(result)("equals is disabled - use === or equivalent instead")
  }

  test("can use custom equals") {
    val result = WartTestTraverser(Equals) {
      java.util.Arrays.equals(Array(1), Array(1))
    }
    assertEmpty(result)
  }

  test("can't use eq or ne") {
    class Foo(i: Int)

    val result1 = WartTestTraverser(Equals) {
      new Foo(5) eq new Foo(4)
    }
    assertError(result1)("eq is disabled - use === or equivalent instead")

    val result2 = WartTestTraverser(Equals) {
      new Foo(5) ne new Foo(4)
    }
    assertError(result2)("ne is disabled - use =/= or equivalent instead")
  }

}
