package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.Unsafe

class PartialFunctionTest extends FunSuite {
  test("can use partial functions") {
    val result = WartTestTraverser(Unsafe) {
      val f1: PartialFunction[Int, Int] = {
        case 3 => 4
      }

      def f2(f: PartialFunction[Int, Int]) = ()
      f2 {
        case 3 => 4
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
