package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Return

class ReturnTest extends FunSuite {
  test("local return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(n:Int): Int = return n + 1
    }
    expectResult(List("return is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("nonlocal return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(ns: List[Int]): Any = ns.map(n => return n + 1)
    }
    expectResult(List("return is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
