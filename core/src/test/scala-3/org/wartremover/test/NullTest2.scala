package org.wartremover
package test

import org.wartremover.warts.Null
import org.scalatest.funsuite.AnyFunSuite

class NullTest2 extends AnyFunSuite with ResultAssertions {
  test("compiletime.uninitialized AnyRef") {
    val result = WartTestTraverser(Null) {
      class c {
        var s: String = compiletime.uninitialized
      }
    }
    assertError(result)("null is disabled")
  }

  test("compiletime.uninitialized primitive") {
    val result = WartTestTraverser(Null) {
      class c {
        var b: Boolean = compiletime.uninitialized
        var i: Int = compiletime.uninitialized
        var u: Unit = compiletime.uninitialized
      }
    }
    assertEmpty(result)
  }
}
