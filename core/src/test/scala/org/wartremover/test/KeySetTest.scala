package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.KeySet

class KeySetTest extends AnyFunSuite with ResultAssertions {
  test("report error") {
    Seq(
      WartTestTraverser(KeySet) {
        Map.empty[Int, Int].map(a => a._1).toSet
      },
      WartTestTraverser(KeySet) {
        collection.Map.empty[Int, Int].map(_._1).toSet
      },
      WartTestTraverser(KeySet) {
        collection.immutable.Map.empty[Int, Int].map(_._1).toSet
      },
      WartTestTraverser(KeySet) {
        collection.mutable.Map.empty[Int, Int].map(_._1).toSet
      },
      WartTestTraverser(KeySet) {
        collection.concurrent.TrieMap.empty[Int, Int].map(_._1).toSet
      },
      WartTestTraverser(KeySet) {
        collection.immutable.IntMap.empty[Int].map(_._1).toSet
      },
    ).foreach { result =>
      assertError(result)("you can use keySet")
    }
  }

  test("not report error if not Map") {
    val result = WartTestTraverser(KeySet) {
      Seq.empty[(Int, Int)].map(_._1).toSet
      List.empty[(Int, Int)].map(_._1).toSet
      Iterable.empty[(Int, Int)].map(_._1).toSet
      Vector.empty[(Int, Int)].map(_._1).toSet
    }
    assertEmpty(result)
  }

  test("not report error if not only _1") {
    val result = WartTestTraverser(KeySet) {
      Map.empty[Int, Int].map(_._2).toSet
      Map.empty[Int, Int].map(_ => "a").toSet
      Map.empty[(Int, Int), Int].map(_._1._1).toSet
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(KeySet) {
      @SuppressWarnings(Array("org.wartremover.warts.KeySet"))
      def f = Seq(
        collection.Map.empty[Int, Int].map(_._1).toSet,
        collection.immutable.Map.empty[Int, Int].map(_._1).toSet,
        collection.mutable.Map.empty[Int, Int].map(_._1).toSet,
        collection.concurrent.TrieMap.empty[Int, Int].map(_._1).toSet,
        collection.immutable.IntMap.empty[Int].map(_._1).toSet,
      )
    }
    assertEmpty(result)
  }
}
