package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.DefaultArguments

class DefaultArgumentsTest extends FunSuite {
  test("Default arguments can't be used") {
    val result = WartTestTraverser(DefaultArguments) {
      def x(y: Int = 4) = y
    }
    expectResult(List("Function has default arguments"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
