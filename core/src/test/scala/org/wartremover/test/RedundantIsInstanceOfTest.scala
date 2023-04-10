package org.wartremover
package test

import org.wartremover.warts.RedundantIsInstanceOf
import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.test.RedundantIsInstanceOfTest._
import scala.annotation.nowarn

class RedundantIsInstanceOfTest extends AnyFunSuite with ResultAssertions {
  private def x1: Int = 2

  private def a1: A1 = new A1
  private def a2: A2 = new A2

  test("can't use isInstanceOf") {
    List(
      WartTestTraverser(RedundantIsInstanceOf) {
        3.isInstanceOf[Int]
      },
      WartTestTraverser(RedundantIsInstanceOf) {
        x1.isInstanceOf[Any]
      },
      WartTestTraverser(RedundantIsInstanceOf) {
        a1.isInstanceOf[A1]
      },
      WartTestTraverser(RedundantIsInstanceOf) {
        a2.isInstanceOf[A1]
      },
    ).foreach { result =>
      assertError(result)("redundant isInstanceOf")
    }
  }

  test("can use isInstanceOf if not sub types") {
    val result = WartTestTraverser(RedundantIsInstanceOf) {
      @nowarn("msg=fruitless type test")
      def y1 = x1.isInstanceOf[String]
      x1.isInstanceOf[Long]
      a1.isInstanceOf[A2]
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(RedundantIsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.RedundantIsInstanceOf"))
      def x = a1.isInstanceOf[A1]
    }
    assertEmpty(result)
  }
}

object RedundantIsInstanceOfTest {
  private class A1
  private class A2 extends A1
}
