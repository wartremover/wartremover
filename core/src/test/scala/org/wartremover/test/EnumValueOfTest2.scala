package org.wartremover
package test

import org.wartremover.warts.EnumValueOf
import org.scalatest.funsuite.AnyFunSuite

class EnumValueOfTest2 extends AnyFunSuite with ResultAssertions {
  test("valueOf is disabled") {
    Seq(
      WartTestTraverser(EnumValueOf) {
        java.util.concurrent.TimeUnit.valueOf("")
      },
      WartTestTraverser(EnumValueOf) {
        TestEnum.valueOf("")
      }
    ).foreach(result => assertError(result)("Enum.valueOf is disabled"))
  }

  test("not Enum valueOf") {
    val result = WartTestTraverser(EnumValueOf) {
      EnumValueOfTest2.B.valueOf("b")
    }
    assertEmpty(result)
  }

  test("Java enum overload") {
    val result = WartTestTraverser(EnumValueOf) {
      TestEnum.valueOf(2)
      TestEnum.valueOf("1", "2")
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(EnumValueOf) {
      @SuppressWarnings(Array("org.wartremover.warts.EnumValueOf"))
      def f() = {
        java.util.concurrent.TimeUnit.valueOf("c")
        TestEnum.valueOf("")
      }
    }
    assertEmpty(result)
  }
}

object EnumValueOfTest2 {
  private object B {
    def valueOf(s: String): B = new B
  }

  private class B
}
