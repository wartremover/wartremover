package org.wartremover
package test

import org.wartremover.warts.Unsafe
import org.scalatest.funsuite.AnyFunSuite

class CaseClassTest2 extends AnyFunSuite with ResultAssertions {
  test("case classes with type param still work") {
    val result = WartTestTraverser(Unsafe) {
      case class B[X](a: X)
    }
    assertEmpty(result)
  }
}
