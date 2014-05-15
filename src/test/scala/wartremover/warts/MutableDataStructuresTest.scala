package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.MutableDataStructures

class MutableDataStructuresTest extends FunSuite {
  test("disable scala.collection.mutable._ when referenced") {
    val result = WartTestTraverser(MutableDataStructures) {
      var x = scala.collection.mutable.HashMap("key" -> "value")
    }
    expectResult(List("scala.collection.mutable package is disabled"), "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
  test("ignore immutable collections") {
    val result = WartTestTraverser(MutableDataStructures) {
      var x = Map("key" -> "value")
    }
    expectResult(List.empty, "result.errors")(result.errors)
    expectResult(List.empty, "result.warnings")(result.warnings)
  }
}
