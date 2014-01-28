package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.AsInstanceOf

class AsInstanceOfTest extends FunSuite {
  test("asInstanceOf is disabled") {
    val result = WartTestTraverser(AsInstanceOf) {
      "abc".asInstanceOf[String]
    }
    assert(result.errors == List("asInstanceOf is disabled"))
    assert(result.warnings == List.empty)
  }
  test("asInstanceOf wart doesn't break type patterns") {
    val result = WartTestTraverser(AsInstanceOf) {
      def foo(a: Any) = a match {
        case n: Int    => n + 1
        case s: String => s.length
      }
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("asInstanceOf wart doesn't break safe constructor patterns") {
    val result = WartTestTraverser(AsInstanceOf) {
      def foo(a: Option[Int]) = a match {
        case Some(n) => n + 1
        case None    => 2
      }
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
  test("asInstanceOf wart doesn't break unsafe constructor patterns") {
    val result = WartTestTraverser(AsInstanceOf) {
      def foo(a: Any) = a match {
        case Some(n : Int) => n + 1
        case b :: bs       => bs.length
      }
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
