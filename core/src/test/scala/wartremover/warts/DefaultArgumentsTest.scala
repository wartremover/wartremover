package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.DefaultArguments

class DefaultArgumentsTest extends FunSuite {
  test("Default arguments can't be used") {
    val result = WartTestTraverser(DefaultArguments) {
      def x(y: Int = 4) = y
    }
    assertResult(List("Function has default arguments"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Default arguments wart obeys ignoreWarts") {
    val result = WartTestTraverser(DefaultArguments) {
      @ignoreWarts("org.brianmckenna.wartremover.warts.DefaultArguments")
      def x(y: Int = 4) = y
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
