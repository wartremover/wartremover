package org.brianmckenna.wartremover
package test

import java.lang.SuppressWarnings
import org.scalatest.FunSuite
import org.brianmckenna.wartremover.warts.FloatingPointNumericRange

class FloatingPointNumericRangeTest extends FunSuite {

  private def shouldLint(result: WartTestTraverser.Result) = {
    val msg = "Do not use NumericRange for floating point Numbers, it's broken"
    assertResult(List(msg), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  private def shouldNotLint(result: WartTestTraverser.Result) = {
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("prohibit use of NumericRange created inline") {
    shouldLint(WartTestTraverser(FloatingPointNumericRange) {
      1D to 8D by 0.5D
    })
  }

  test("prohibit use of NumericRange created before") {
    shouldLint(WartTestTraverser(FloatingPointNumericRange) {
      val r = 1D to 8D
      r by 0.5D
    })
  }

  test("don't raise false error due to by method") {
    shouldNotLint(WartTestTraverser(FloatingPointNumericRange) {
      object Bye {
        def by(d: Double) = d
      }
      Bye by 8D
    })
  }

  test("don't raise false error due to non-by method") {
    shouldNotLint(WartTestTraverser(FloatingPointNumericRange) {
      (1D to 8D).##
    })
  }

  test("obey suppress warning annotation") {
    shouldNotLint(WartTestTraverser(FloatingPointNumericRange) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.FloatingPointNumericRange"))
      val range = 1D to 8D by 0.5D
    })
  }

}
