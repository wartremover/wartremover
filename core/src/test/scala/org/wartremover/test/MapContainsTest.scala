package org.wartremover
package test

import org.wartremover.warts.MapContains
import org.scalatest.funsuite.AnyFunSuite

class MapContainsTest extends AnyFunSuite with ResultAssertions {
  private def message = "Maybe you can use `contains`"

  test("report error") {
    List(
      WartTestTraverser(MapContains) {
        Map.empty[Int, Int].get(2).isEmpty
      },
      WartTestTraverser(MapContains) {
        collection.mutable.Map.empty[Int, Int].get(2).nonEmpty
      },
      WartTestTraverser(MapContains) {
        collection.Map.empty[Int, Int].get(2).isDefined
      },
      WartTestTraverser(MapContains) {
        collection.concurrent.TrieMap.empty[Int, Int].get(2).isEmpty
      },
      WartTestTraverser(MapContains) {
        collection.immutable.TreeMap.empty[Int, Int].get(2).nonEmpty
      },
    ).foreach { result =>
      assertError(result)(message)
    }
  }

  test("don't report error if not Map class") {
    trait Foo {
      def get(x: Int): Option[Int]
    }
    val result = WartTestTraverser(MapContains) {
      def f(x: Foo): Boolean = x.get(2).isEmpty
    }
    assertEmpty(result)
  }

  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(MapContains) {
      @SuppressWarnings(Array("org.wartremover.warts.MapContains"))
      def x = Map.empty[Int, Int].get(2).isEmpty
    }
    assertEmpty(result)
  }
}
