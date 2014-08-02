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

  test("can't use List#last on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().last)
    }
    expectResult(List("List#last is disabled - use List#lastOption instead"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }

}
