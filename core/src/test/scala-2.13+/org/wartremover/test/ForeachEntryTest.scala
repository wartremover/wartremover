package org.wartremover
package test

import org.wartremover.warts.ForeachEntry
import org.scalatest.funsuite.AnyFunSuite

class ForeachEntryTest extends AnyFunSuite with ResultAssertions {
  private def collectionMap: collection.Map[Int, String] = ???
  private def immutableMap: collection.immutable.Map[Int, String] = ???
  private def mutableMap: collection.mutable.Map[Int, String] = ???
  private def seq: collection.Seq[(Int, String)] = ???

  test("report error") {
    Seq(
      WartTestTraverser(ForeachEntry) {
        collectionMap.foreach { case (k, v) => k }
      },
      WartTestTraverser(ForeachEntry) {
        immutableMap.foreach { case (k, v) => v }
      },
      WartTestTraverser(ForeachEntry) {
        mutableMap.foreach { case (k, v) => 8 }
      },
      WartTestTraverser(ForeachEntry) {
        collectionMap.foreach {
          case (k, v) if k % 3 == 1 => k + 1
          case (2, v) => 0
          case (k, v) => k
        }
      }
    ).foreach { result =>
      assertError(result)(
        "You can use `foreachEntry` instead of `foreach` if Scala 2.13+"
      )
    }
  }

  test("don't report error") {
    val result = WartTestTraverser(ForeachEntry) {
      collectionMap.foreach { case x @ (k, v) => }
      collectionMap.foreach { x => }
      immutableMap.foreach { x => x }
      mutableMap.foreach { x => }
      seq.foreach { case (k, v) => k }
    }
    assertEmpty(result)
  }

  test("ForeachEntry wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ForeachEntry) {
      @SuppressWarnings(Array("org.wartremover.warts.ForeachEntry"))
      def foo = {
        collectionMap.foreach { case (k, v) => k }
        immutableMap.foreach { case (k, v) => v }
        mutableMap.foreach { case (k, v) => "x" }
      }
    }
    assertEmpty(result)
  }
}
