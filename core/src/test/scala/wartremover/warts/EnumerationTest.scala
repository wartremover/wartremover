package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.{ Enumeration => EnumerationWart }

class EnumerationTest extends FunSuite with ResultAssertions {
  test("can't declare Enumeration classes") {
    val result = WartTestTraverser(EnumerationWart) {
      class Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertError(result)("[org.wartremover.warts.Enumeration] Enumeration is disabled - use case objects instead")
  }
  test("can't declare Enumeration objects") {
    val result = WartTestTraverser(EnumerationWart) {
      object Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertError(result)("[org.wartremover.warts.Enumeration] Enumeration is disabled - use case objects instead")
  }
  test("can use user-defined Enumeration traits") {
    val result = WartTestTraverser(EnumerationWart) {
      trait Enumeration
      object Foo extends Enumeration
    }
    assertEmpty(result)
  }
  test("Enumeration wart obeys SuppressWarnings") {
    val result = WartTestTraverser(EnumerationWart) {
      @SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
      object Color extends Enumeration {
        val Red = Value
        val Blue = Value
      }
    }
    assertEmpty(result)
  }
}
