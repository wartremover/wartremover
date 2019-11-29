package org.wartremover
package test


import org.wartremover.warts.DefaultArguments
import org.scalatest.funsuite.AnyFunSuite

class DefaultArgumentsTest extends AnyFunSuite with ResultAssertions {
  test("Default arguments can't be used") {
    val result = WartTestTraverser(DefaultArguments) {
      def x(y: Int = 4) = y
    }
    assertError(result)("Function has default arguments")
  }
  test("Default arguments wart obeys SuppressWarnings") {
    val result = WartTestTraverser(DefaultArguments) {
      @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
      def x(y: Int = 4) = y
    }
    assertEmpty(result)
  }
}
