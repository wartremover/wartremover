package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.ListOps

class ListTest extends FunSuite {
  test("can't use List#head on List") {
    val result = WartTestTraverser(ListOps) {
      println(List(1).head)
    }
    expectResult(List("List#head is disabled - use List#headOption instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#tail on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().tail)
    }
    expectResult(List("List#tail is disabled - use List#drop(1) instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#init on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().init)
    }
    expectResult(List("List#init is disabled - use List#dropRight(1) instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#last on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().last)
    }
    expectResult(List("List#last is disabled - use List#lastOption instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#reduce on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduce(_ + _))
    }
    expectResult(List("List#reduce is disabled - use List#reduceOption or List#fold instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#reduceLeft on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceLeft(_ + _))
    }
    expectResult(List("List#reduceLeft is disabled - use List#reduceLeftOption or List#foldLeft instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#reduceRight on List") {
    val result = WartTestTraverser(ListOps) {
      println(List.empty[Int].reduceRight(_ + _))
    }
    expectResult(List("List#reduceRight is disabled - use List#reduceRightOption or List#foldRight instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

}
