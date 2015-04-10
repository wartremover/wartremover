package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.FinalCaseClass

class FinalCaseClassTest extends FunSuite {
  test("can't declare nonfinal case classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      case class Foo(i: Int)
    }
    assertResult(List("case classes must be final"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can declare final case classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      final case class Foo(i: Int)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can declare nonfinal regular classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      class Foo(i: Int)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can declare nonfinal case classes inside other classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      class Outer {
        case class Foo(i: Int)
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("FinalCaseClass wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FinalCaseClass) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.FinalCaseClass"))
      case class Foo(i: Int)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
