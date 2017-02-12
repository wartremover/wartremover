package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.UnsafeInheritance

class UnsafeInheritanceTest extends FunSuite {
  test("Method must be final or abstract") {
    val result = WartTestTraverser(UnsafeInheritance) {
      trait T {
        def m() = {}
      }
    }
    assertResult(List("Method must be final or abstract"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("Final and non-final abstract, private, object methods are allowed") {
    val result = WartTestTraverser(UnsafeInheritance) {
      trait T {
        final def m2() = {}
        def m1(): Unit
        private def m3() = {}
      }
      final class C1 {
        def m() = {}
      }
      sealed class C2 {
        def m() = {}
      }
      object O {
        def m() = {}
      }
      case class CC(i: Int)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("UnsafeInheritance wart obeys SuppressWarnings") {
    val result = WartTestTraverser(UnsafeInheritance) {
      trait T {
        @SuppressWarnings(Array("org.wartremover.warts.UnsafeInheritance"))
        def m() = {}
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
