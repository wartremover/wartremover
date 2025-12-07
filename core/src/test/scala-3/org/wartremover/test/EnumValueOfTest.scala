package org.wartremover
package test

import org.wartremover.warts.EnumValueOf
import org.scalatest.funsuite.AnyFunSuite

class EnumValueOfTest extends AnyFunSuite with ResultAssertions {
  test("valueOf is disabled") {
    Seq(
      WartTestTraverser(EnumValueOf) {
        EnumValueOfTest.A.valueOf("b")
      },
      WartTestTraverser(EnumValueOf) {
        EnumValueOfTest.A.valueOf(???)
      }
    ).foreach(result => assertError(result)("Enum.valueOf is disabled"))
  }

  test("not Enum valueOf") {
    val result = WartTestTraverser(EnumValueOf) {
      EnumValueOfTest.B.valueOf("b")
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(EnumValueOf) {
      @SuppressWarnings(Array("org.wartremover.warts.EnumValueOf"))
      def f() = EnumValueOfTest.A.valueOf("c")
    }
    assertEmpty(result)
  }
}

object EnumValueOfTest {
  private enum A {
    case A1
  }

  private object B {
    def valueOf(s: String): B = new B
  }
  private class B
}
