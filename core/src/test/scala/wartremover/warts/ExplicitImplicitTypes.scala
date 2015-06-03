package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.ExplicitImplicitTypes

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
    }
    assertResult(List("implicit definitions must have an explicit type ascription"), "result.errors")(result.errors)
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
  test("can declare implicit parameters") {
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
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.ExplicitImplicitTypes"))
      implicit val foo = 5

      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.ExplicitImplicitTypes"))
      implicit def bar = 5
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
