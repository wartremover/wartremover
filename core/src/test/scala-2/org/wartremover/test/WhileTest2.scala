package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.While

class WhileTest2 extends AnyFunSuite with ResultAssertions {
  test("do while is disabled") {
    val result = WartTestTraverser(While) {
      do {
        println()
      } while (true)
    }
    assertError(result)("while is disabled")
  }
}
