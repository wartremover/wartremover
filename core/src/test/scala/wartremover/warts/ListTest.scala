package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.ListOps

class ListTest extends FunSuite {
  test("can't use head") {
    val result = WartTestTraverser(ListOps) {
      println(List(1).head)
    }
    assertResult(List("head is disabled - use headOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use tail") {
    val result = WartTestTraverser(ListOps) {
      println(List().tail)
    }
    assertResult(List("tail is disabled - use drop(1) instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use init") {
    val result = WartTestTraverser(ListOps) {
      println(List().init)
    }
    assertResult(List("init is disabled - use dropRight(1) instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use last") {
    val result = WartTestTraverser(ListOps) {
      println(List().last)
    }
    assertResult(List("last is disabled - use lastOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use reduce") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduce(_ + _))
    }
    assertResult(List("reduce is disabled - use reduceOption or fold instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use reduceLeft") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceLeft(_ + _))
    }
    assertResult(List("reduceLeft is disabled - use reduceLeftOption or foldLeft instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use reduceRight") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceRight(_ + _))
    }
    assertResult(List("reduceRight is disabled - use reduceRightOption or foldRight instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("ListOps wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ListOps) {
      @SuppressWarnings(Array("org.wartremover.warts.ListOps"))
      val foo = {
        println(List(1).head)
        println(List().tail)
        println(List().init)
        println(List().last)
        println(List.empty[Int].reduce(_ + _))
        println(List.empty[Int].reduceLeft(_ + _))
        println(List.empty[Int].reduceRight(_ + _))
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

}
