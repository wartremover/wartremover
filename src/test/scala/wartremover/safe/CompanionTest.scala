package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Null

class CompanionTest extends FunSuite {
  test("can use companion objects for case classes") {
    val result = WartTestTraverser(Null) {
      case class Foo(n: Int)
      object Foo {
      }
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
