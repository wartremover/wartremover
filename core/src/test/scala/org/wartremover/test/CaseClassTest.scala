package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

class CaseClassTest extends AnyFunSuite with ResultAssertions {
  test("case classes still work") {
    val result = WartTestTraverser(Unsafe) {
      case class A(a: Int)
    }
    assertEmpty(result)
  }
  test("case classes with type param still work") {
    val result = WartTestTraverser(Unsafe) {
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
