package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.MutableDataStructures

class MutableDataStructuresTest extends FunSuite with ResultAssertions {
  test("disable scala.collection.mutable._ when referenced") {
    val result = WartTestTraverser(MutableDataStructures) {
      var x = scala.collection.mutable.HashMap("key" -> "value")
    }
    assertError(result)("[org.wartremover.warts.MutableDataStructures] scala.collection.mutable package is disabled")
  }
  test("ignore immutable collections") {
    val result = WartTestTraverser(MutableDataStructures) {
      var x = Map("key" -> "value")
    }
    assertEmpty(result)
  }
  test("MutableDataStructures wart obeys SuppressWarnings") {
    val result = WartTestTraverser(MutableDataStructures) {
      @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
      var x = scala.collection.mutable.HashMap("key" -> "value")
    }
    assertEmpty(result)
  }
}
