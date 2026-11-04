package org.wartremover.test

import org.scalatest.Inspectors
import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.MapPartial

class MapPartialTest extends AnyFunSuite with ResultAssertions with Inspectors {
  test("can't use Map#apply on all kinds of Maps") {
    val result1 = WartTestTraverser(MapPartial) {
      println(Map("a" -> 1)("a"))
    }
    val result2 = WartTestTraverser(MapPartial) {
      println(collection.Map("a" -> 1)("a"))
    }
    val result3 = WartTestTraverser(MapPartial) {
      println(collection.immutable.Map("a" -> 1)("a"))
    }
    val result4 = WartTestTraverser(MapPartial) {
      println(collection.mutable.Map("a" -> 1)("a"))
    }

    forAll(List(result1, result2, result3, result4)) { result =>
      assertError(result)("Map#apply is disabled - use Map#getOrElse instead")
    }
  }

  test("can't use Map#apply without syntax-sugar") {
    val result = WartTestTraverser(MapPartial) {
      println(Map("a" -> 1).apply("a"))
    }

    assertError(result)("Map#apply is disabled - use Map#getOrElse instead")
  }

  test("doesn't detect other `apply` methods") {
    val result = WartTestTraverser(MapPartial) {
      class A {
        def apply(i: Int) = 3
      }
      println((new A).apply(2))
    }

    assertEmpty(result)
  }

  test("MapPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(MapPartial) {
      @SuppressWarnings(Array("org.wartremover.warts.MapPartial"))
      val foo = {
        println(Map("a" -> 1)("a"))
      }
    }

    assertEmpty(result)
  }
}
