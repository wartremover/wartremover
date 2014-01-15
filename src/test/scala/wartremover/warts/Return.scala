package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Return

class ReturnTest extends FunSuite {
  test("local return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(n:Int): Int = return n + 1
    }
    assert(result.errors == List("return is disabled"))
    assert(result.warnings == List.empty)
  }
  test("nonlocal return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(ns: List[Int]): Any = ns.map(n => return n + 1)
    }
    assert(result.errors == List("return is disabled"))
    assert(result.warnings == List.empty)
  }
}
