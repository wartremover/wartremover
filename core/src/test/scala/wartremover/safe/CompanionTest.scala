package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class CompanionTest extends FunSuite with ResultAssertions {
  test("can use companion objects for type aliases") {
    val result = WartTestTraverser(Unsafe) {
      trait T[R]
      type T1 = String
      object T1 extends T[Unit]
    }
    assertEmpty(result)
  }
}
