package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.ListOps

class ListTest extends FunSuite {
  test("can't use List#head on List") {
    val result = WartTestTraverser(ListOps) {
      println(List(1).head)
    }
    assertResult(List("List#head is disabled - use List#headOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#tail on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().tail)
    }
    assertResult(List("List#tail is disabled - use List#drop(1) instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  test("can't use List#last on List") {
    val result = WartTestTraverser(ListOps) {
      println(List().last)
    }
    assertResult(List("List#last is disabled - use List#lastOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

}
