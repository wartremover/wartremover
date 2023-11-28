package org.wartremover
package test

import org.wartremover.warts.Null
import org.scalatest.funsuite.AnyFunSuite
import scala.annotation.nowarn

class NowarnAnnotationTest extends AnyFunSuite with ResultAssertions {

  test("wart obeys scala.annotation.nowarn") {
    val result = WartTestTraverser(Null) {
      @nowarn
      val f1 = {
        println(null)
        val (a, b) = (1, null)
        println(a)
        Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
      }

      @nowarn("")
      val f2 = {
        println(null)
        val (a, b) = (1, null)
        println(a)
        Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
      }
    }
    assertEmpty(result)
  }

  test("report error if scala.annotation.nowarn with args") {
    val result = WartTestTraverser(Null) {
      @nowarn("msg=aaaaaa")
      val f1 = {
        println(null)
        val (a, b) = (1, null)
        println(a)
        Map(1 -> "one", 2 -> "two").partition { case (k, v) => null.asInstanceOf[Boolean] }
      }
    }
    assertErrors(result)("null is disabled", 3)
  }
}
