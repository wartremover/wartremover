package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

class CaseClassTest extends FunSuite {
  test("case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int)
      case class B[X](a: X)
    }
    assert(result.errors == List.empty)
    assert(result.warnings == List.empty)
  }
}
