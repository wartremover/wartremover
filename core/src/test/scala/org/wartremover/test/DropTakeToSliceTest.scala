package org.wartremover
package test

import org.wartremover.warts.DropTakeToSlice
import org.scalatest.funsuite.AnyFunSuite

class DropTakeToSliceTest extends AnyFunSuite with ResultAssertions {
  test("drop.take disabled") {
    val result = WartTestTraverser(DropTakeToSlice) {
      List(1, 2, 3).drop(1).take(2)
    }
    assertError(result)("you can use slice instead of drop.take")
  }

  test("DropTakeToSlice wart obeys SuppressWarnings") {
    val result = WartTestTraverser(DropTakeToSlice) {
      @SuppressWarnings(Array("org.wartremover.warts.DropTakeToSlice"))
      def x = List(1, 2, 3).drop(1).take(2)
    }
    assertEmpty(result)
  }
}
