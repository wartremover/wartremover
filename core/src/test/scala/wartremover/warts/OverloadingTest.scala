package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Overloading

class OverloadingTest extends FunSuite with ResultAssertions {
  test("Overloading is disabled") {
    val result = WartTestTraverser(Overloading) {
      class c {
        def f(i: Int) = {}
        def f(s: String) = {}
        def wait(s: String) = {}
      }
    }
    assertErrors(result)("Overloading is disabled", 3)
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
    assertEmpty(result)
  }

  test("Overloading wart obeys SuppressWarnings on defs") {
    val result = WartTestTraverser(Overloading) {
      class c {
        def f(i: Int) = {}
        @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
        def f(s: String) = {}
      }
    }
    assertEmpty(result)
  }
}
