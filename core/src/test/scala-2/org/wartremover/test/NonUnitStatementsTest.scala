package org.wartremover
package test

import org.wartremover.warts.NonUnitStatements
import org.scalatest.funsuite.AnyFunSuite

// TODO Scala 3 ?
class NonUnitStatementsTest extends AnyFunSuite with ResultAssertions {
  test("non-unit statements are disabled") {
    val result = WartTestTraverser(NonUnitStatements) {
      1
      2
    }
    assertError(result)("Statements must return Unit")
  }
  test("Extending a class with multiple parameter lists doesn't fail") {
    val result = WartTestTraverser(NonUnitStatements) {
      class A(x: Int)(y: Int)(z: Int)
      class B(x: Int)(y: Int)(z: Int) extends A(x)(y)(z)
    }
    assertEmpty(result)
  }
  test("XML literals don't fail") {
    val result = WartTestTraverser(NonUnitStatements) {
      val a = 13
      <x>{a}</x>
    }
    assertEmpty(result)
  }
  test("&+ method but not xml") {
    class A {
      def &+(i: Int): A = this
    }
    val result = WartTestTraverser(NonUnitStatements) {
      val a = new A
      a &+ 42
      a
    }
    assertError(result)("Statements must return Unit")
  }

  test("NonUnitStatements wart obeys SuppressWarnings") {
    val result = WartTestTraverser(NonUnitStatements) {
      @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
      val foo = {
        1
        2
      }
    }
    assertEmpty(result)
  }
}
