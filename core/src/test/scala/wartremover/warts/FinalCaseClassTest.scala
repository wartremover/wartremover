package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.FinalCaseClass

class FinalCaseClassTest extends FunSuite with ResultAssertions {
  test("can't declare nonfinal case classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      case class Foo(i: Int)
    }
    assertError(result)("[org.wartremover.warts.FinalCaseClass] case classes must be final")
  }

  test("can declare final case classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      final case class Foo(i: Int)
    }
    assertEmpty(result)
  }

  test("can declare nonfinal sealed case classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      sealed case class Foo(i: Int)
    }
    assertEmpty(result)
  }

  test("can declare nonfinal regular classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      class Foo(i: Int)
    }
    assertEmpty(result)
  }

  test("can declare nonfinal case classes inside other classes") {
    val result = WartTestTraverser(FinalCaseClass) {
      class Outer {
        case class Foo(i: Int)
      }
    }
    assertEmpty(result)
  }

  test("FinalCaseClass wart obeys SuppressWarnings") {
    val result = WartTestTraverser(FinalCaseClass) {
      @SuppressWarnings(Array("org.wartremover.warts.FinalCaseClass"))
      case class Foo(i: Int)
    }
    assertEmpty(result)
  }
}
