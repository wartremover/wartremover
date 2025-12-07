package org.wartremover
package test

import org.wartremover.test.CaseClassPrivateApplyTest._
import org.wartremover.warts.CaseClassPrivateApply
import org.scalatest.funsuite.AnyFunSuite

class CaseClassPrivateApplyTest extends AnyFunSuite with ResultAssertions {
  test("disable apply") {
    Seq(
      WartTestTraverser(CaseClassPrivateApply) {
        A1(2)
      },
      WartTestTraverser(CaseClassPrivateApply) {
        A2(3)
      },
      WartTestTraverser(CaseClassPrivateApply) {
        A3(4)
      }
    ).foreach { result =>
      assertError(result)("disable apply because constructor is private")
    }
  }

  test("allow apply") {
    val result = WartTestTraverser(CaseClassPrivateApply) {
      B1(2)
      B2(3)
      B3(4)
      B4(5)
    }
    assertEmpty(result)
  }

  test("obeys SuppressWarnings") {
    val result = WartTestTraverser(CaseClassPrivateApply) {
      @SuppressWarnings(Array("org.wartremover.warts.CaseClassPrivateApply"))
      def f = (
        A1(2),
        A2(3),
        A3(4)
      )
    }
    assertEmpty(result)
  }
}

object CaseClassPrivateApplyTest {
  case class A1 private (x: Int)

  case class A2 private (x: Int)
  object A2

  case class A3 private (x: Int)
  object A3 {
    def apply(x1: Int, x2: Int): A3 = new A3(x1 + x2)

    // TODO
    // def apply(x: String): A3 = new A3(x.toInt)
  }

  case class B1(x: Int)

  case class B2 private[wartremover] (x: Int)

  case class B3 private (x: Int)
  object B3 {
    def apply(x: Int): B3 = new B3(x)
  }

  object B4 {
    def apply(x: Int): Int = x
  }
}
