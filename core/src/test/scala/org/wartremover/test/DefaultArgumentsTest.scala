package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.DefaultArguments

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

      @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
      case class A(y: Int = 2)
    }
    assertEmpty(result)
  }
}
