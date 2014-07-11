package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Unsafe

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
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
