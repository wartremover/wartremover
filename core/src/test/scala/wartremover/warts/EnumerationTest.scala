package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.{ Enumeration => EnumerationWart }

class EnumerationTest extends FunSuite {
  test("can't declare Enumeration classes") {
    val result = WartTestTraverser(EnumerationWart) {
      class Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertResult(List("Enumeration is disabled - use case objects instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't declare Enumeration objects") {
    val result = WartTestTraverser(EnumerationWart) {
      object Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertResult(List("Enumeration is disabled - use case objects instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can use user-defined Enumeration traits") {
    val result = WartTestTraverser(EnumerationWart) {
      trait Enumeration
      object Foo extends Enumeration
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Enumeration wart obeys SuppressWarnings") {
    val result = WartTestTraverser(EnumerationWart) {
      @SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
      object Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
