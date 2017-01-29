package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class CaseClassTest extends FunSuite with ResultAssertions {
  test("case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int)
      case class B[X](a: X)
    }
    assertEmpty(result)
  }
  test("vararg case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int*)
    }
    assertEmpty(result)
  }
}
