package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.MissingOverride

class MissingOverrideTest extends FunSuite with ResultAssertions {
  test("Method must have override modifier") {
    val result = WartTestTraverser(MissingOverride) {
      trait T {
        def f(): Unit
      }
      class C extends T {
        def f() = {}
      }
    }
    assertError(result)("Method must have override modifier")
  }

  test("Explicit override is allowed") {
    val result = WartTestTraverser(MissingOverride) {
      trait T {
        def f(): Unit
      }
      class C extends T {
        override def f() = {}
      }
    }
    assertEmpty(result)
  }

  test("MissingOverride wart obeys SuppressWarnings") {
    val result = WartTestTraverser(MissingOverride) {
      trait T {
        def f(): Unit
      }
      class C extends T {
        @SuppressWarnings(Array("org.wartremover.warts.MissingOverride"))
        def f() = {}
      }
    }
    assertEmpty(result)
  }
}
