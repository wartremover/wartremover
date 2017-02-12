package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Equals

class EqualsTest extends FunSuite with ResultAssertions {
  test("can't use == or != method on primitives") {
    val result = WartTestTraverser(Equals) {
      val s = "foo"
      val i = 5
      i == s
      i != s
    }
    assertResult(List("[org.wartremover.warts.Equals] == is disabled - use === or equivalent instead", "[org.wartremover.warts.Equals] != is disabled - use =/= or equivalent instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use == or != on classes") {
    val result = WartTestTraverser(Equals) {
      class Foo(i: Int)
		new Foo(5) == new Foo(4)
		new Foo(5) != new Foo(4)
    }
    assertResult(List("[org.wartremover.warts.Equals] == is disabled - use === or equivalent instead", "[org.wartremover.warts.Equals] != is disabled - use =/= or equivalent instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use == or != on case classes") {
    val result = WartTestTraverser(Equals) {
      case class Foo(i: Int)
		Foo(5) == Foo(4)
		Foo(5) != Foo(4)
    }
    assertResult(List("[org.wartremover.warts.Equals] == is disabled - use === or equivalent instead", "[org.wartremover.warts.Equals] != is disabled - use =/= or equivalent instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
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
}
