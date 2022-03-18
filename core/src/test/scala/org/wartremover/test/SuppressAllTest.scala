package org.wartremover
package test

import org.wartremover.warts.{Var, Null, Return}
import org.scalatest.funsuite.AnyFunSuite

class SuppressAllTest extends AnyFunSuite with ResultAssertions {
  test("all warts obeys SuppressWarnings") {
    val result1 = WartTestTraverser(Var) {
      @SuppressWarnings(Array("org.wartremover.warts.All"))
      var x = 10
      x
    }
    assertEmpty(result1)

    val result2 = WartTestTraverser(Null) {
      @SuppressWarnings(Array("org.wartremover.warts.All"))
      var x = null
    }
    assertEmpty(result2)

    val result3 = WartTestTraverser(Return) {
      @SuppressWarnings(Array("org.wartremover.warts.All"))
      def foo: Int = {
        var i = 0
        while (true) {
          if (i == 10) {
            return i
          }
          i += 1
        }
        0
      }
    }
    assertEmpty(result3)
  }
}
