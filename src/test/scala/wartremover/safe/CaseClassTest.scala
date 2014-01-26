package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Any

class CaseClassTest extends FunSuite {
  test("case classes still work") {
    val result = WartTestTraverser(Any) {
      case class A(a: Int)
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
