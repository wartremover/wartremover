package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ExplicitImplicitTypes

class ExplicitImplicitTypesTest extends FunSuite {
  test("can't declare implicit vals without a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit val foo = 5
    }
    assertResult(List("implicit definitions must have an explicit type ascription"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't declare implicit defs without a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit def foo = 5
      implicit def bar[A] = 5
      implicit def baz(i: Int) = 5
      implicit def qux[I](i: I) = 5
    }
    assertResult(List(
      "implicit definitions must have an explicit type ascription",
      "implicit definitions must have an explicit type ascription",
      "implicit definitions must have an explicit type ascription",
      "implicit definitions must have an explicit type ascription"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can declare implicit classes") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit class Foo(i : Int) {
        def bar = 2
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can declare implicit vals with a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit val foo: Int = 5
      implicit var bar: Int = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't declare implicit defs with a type ascription") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      implicit def foo: Int = 5
      implicit def bar[A]: Int = 5
      implicit def baz(i: Int): Int = 5
      implicit def qux[I](i: I): Int = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can declare implicit arguments") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      Option(1).map { implicit i =>
        i
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can declare non-implicit vals") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      val foo = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can declare non-implicit defs") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      def foo = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("ExplicitImplicitTypes wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
      implicit val foo = 5

      @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
      implicit def bar = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
