package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Return

class ReturnTest extends FunSuite with ResultAssertions {
  test("local return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(n:Int): Int = return n + 1
    }
    assertError(result)("[org.wartremover.warts.Return] return is disabled")
  }
  test("nonlocal return is disabled") {
    val result = WartTestTraverser(Return) {
      def foo(ns: List[Int]): Any = ns.map(n => return n + 1)
    }
    assertError(result)("[org.wartremover.warts.Return] return is disabled")
  }
  test("Return wart is disabled") {
    val result = WartTestTraverser(Return) {
      @SuppressWarnings(Array("org.wartremover.warts.Return"))
      def foo(n:Int): Int = return n + 1
      @SuppressWarnings(Array("org.wartremover.warts.Return"))
      def bar(ns: List[Int]): Any = ns.map(n => return n + 1)
    }
    assertEmpty(result)
  }
}
