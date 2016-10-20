package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.SymbolicName

class SymbolicNameTest extends FunSuite {
  test("symbolic names are disabled") {
    val result = WartTestTraverser(SymbolicName) {
      class |+|
    }
    assertResult(List("Symbolic name is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("short symbolic names are allowed") {
    val result = WartTestTraverser(SymbolicName) {
      def ::() = {}
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("all third-party names are allowed") {
    val result = WartTestTraverser(SymbolicName) {
      List(1) ::: List(2)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("SymbolicName wart obeys SuppressWarnings") {
    val result = WartTestTraverser(SymbolicName) {
      @SuppressWarnings(Array("org.wartremover.warts.SymbolicName"))
      def :+:() = {}
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
