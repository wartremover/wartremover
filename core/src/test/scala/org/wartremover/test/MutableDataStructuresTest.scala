package org.wartremover
package test

import org.wartremover.warts.MutableDataStructures
import org.scalatest.funsuite.AnyFunSuite

class MutableDataStructuresTest extends AnyFunSuite with ResultAssertions {
  test("disable scala.collection.mutable._ when referenced") {
    Seq(
      WartTestTraverser(MutableDataStructures) {
        var x = scala.collection.mutable.HashMap("key" -> "value")
      },
      WartTestTraverser(MutableDataStructures) {
        val x = scala.collection.mutable.Seq.empty[Int]
      },
      WartTestTraverser(MutableDataStructures) {
        val x = scala.collection.mutable.Set.empty[String]
      },
      WartTestTraverser(MutableDataStructures) {
        def x = scala.collection.mutable.Buffer.empty[Boolean]
      },
      WartTestTraverser(MutableDataStructures) {
        def x = List.newBuilder[Int]
      }
    ).foreach { result =>
      assertError(result)("scala.collection.mutable package is disabled")
    }
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
