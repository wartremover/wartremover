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
      },
    ).foreach(result => assertError(result)("Enum.valueOf is disabled"))
  }
  test("Scala enum overload") {
    val result = WartTestTraverser(EnumValueOf) {
      EnumValueOfTest.C.valueOf(6)
      EnumValueOfTest.C.valueOf("x1", "x2")
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

  private enum C {
    case C1
  }
  private object C {
    def valueOf(x: Int): C = C1
    def valueOf(s1: String, s2: String): C = C1
  }
}
