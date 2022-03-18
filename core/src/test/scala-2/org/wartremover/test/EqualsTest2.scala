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

  // TODO Scala 3 ?
  test("Equals wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Equals) {
      case class Foo(i: Int)
      @SuppressWarnings(Array("org.wartremover.warts.Equals"))
      val i = Foo(5) == Foo(4)
      @SuppressWarnings(Array("org.wartremover.warts.Equals"))
      val j = Foo(5) != Foo(4)
    }
    assertEmpty(result)
  }

  // TODO Scala 3 ?
  test("Equals should work in synthetic lambdas") {
    val result = WartTestTraverser(Equals) {
      Seq(1, 2, 3).exists(_ == 1)
      Seq(1, 2, 3).exists(n => n != 1)
      Seq("a").collect { case s if s eq "" => s ne "x" }
    }
    assertResult(
      List(
        "[wartremover:Equals] == is disabled - use === or equivalent instead",
        "[wartremover:Equals] != is disabled - use =/= or equivalent instead",
        "[wartremover:Equals] eq is disabled - use === or equivalent instead",
        "[wartremover:Equals] ne is disabled - use =/= or equivalent instead"
      ),
      "result.errors"
    )(result.errors)
  }

  // TODO Scala 3 ?
  test("Equals should work in explicit lambdas") {
    val result = WartTestTraverser(Equals) {
      Seq(1, 2, 3).exists(new Function1[Int, Boolean] { def apply(i: Int): Boolean = i == 1 })
      Seq(1, 2, 3).exists(new Function1[Int, Boolean] { def apply(i: Int): Boolean = i != 1 })
      Seq("a").collect(new PartialFunction[String, String] {
        override def apply(s: String) = {
          s ne ""
          s
        }
        override def isDefined(s: String) = s eq ""
      })
    }
    assertResult(
      List(
        "[wartremover:Equals] == is disabled - use === or equivalent instead",
        "[wartremover:Equals] != is disabled - use =/= or equivalent instead",
        "[wartremover:Equals] ne is disabled - use =/= or equivalent instead",
        "[wartremover:Equals] eq is disabled - use === or equivalent instead"
      ),
      "result.errors"
    )(result.errors)
  }
}
