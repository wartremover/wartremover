package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Overloading

class OverloadingTest extends FunSuite {
  test("Overloading is disabled") {
    val result = WartTestTraverser(Overloading) {
      class c {
        def f(i: Int) = {}
        def f(s: String) = {}
        def wait(s: String) = {}
      }
    }
    assertResult(List.fill(3)("Overloading is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Overloading wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Overloading) {
      @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
      class c {
        def f(i: Int) = {}
        def f(s: String) = {}
        def wait(s: String) = {}
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Overloading wart obeys SuppressWarnings on defs") {
    val result = WartTestTraverser(Overloading) {
      class c {
        def f(i: Int) = {}
        @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
        def f(s: String) = {}
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
