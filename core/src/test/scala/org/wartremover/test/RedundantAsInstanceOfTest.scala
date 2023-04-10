package org.wartremover
package test

import org.wartremover.warts.RedundantAsInstanceOf
import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.test.RedundantAsInstanceOfTest._
import scala.annotation.nowarn

class RedundantAsInstanceOfTest extends AnyFunSuite with ResultAssertions {
  private def x1: Int = 2

  private def a1: A1 = new A1
  private def a2: A2 = new A2

  test("can't use asInstanceOf") {
    List(
      WartTestTraverser(RedundantAsInstanceOf) {
        3.asInstanceOf[Int]
      },
      WartTestTraverser(RedundantAsInstanceOf) {
        x1.asInstanceOf[Int]
      },
      WartTestTraverser(RedundantAsInstanceOf) {
        a1.asInstanceOf[A1]
      },
    ).foreach { result =>
      assertError(result)("redundant asInstanceOf")
    }
  }

  test("can use asInstanceOf if different types") {
    val result = WartTestTraverser(RedundantAsInstanceOf) {
      @nowarn("msg=fruitless type test")
      def y1 = x1.asInstanceOf[String]
      x1.asInstanceOf[Long]
      a1.asInstanceOf[A2]
      a2.asInstanceOf[A1]
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(RedundantAsInstanceOf) {
      @SuppressWarnings(Array("org.wartremover.warts.RedundantAsInstanceOf"))
      def x = a1.asInstanceOf[A1]
    }
    assertEmpty(result)
  }
}

object RedundantAsInstanceOfTest {
  private class A1
  private class A2 extends A1
}
