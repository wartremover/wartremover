package org.wartremover
package test

import org.wartremover.warts.ForeachEntry
import org.scalatest.funsuite.AnyFunSuite

class ForeachEntryTest3 extends AnyFunSuite with ResultAssertions {
  private def collectionMap: collection.Map[Int, String] = ???

  test("report error") {
    Seq(
      WartTestTraverser(ForeachEntry) {
        collectionMap.foreach { (k, v) => k }
      },
      WartTestTraverser(ForeachEntry) {
        collectionMap.foreach((k, v) => k)
      }
    ).foreach(result =>
      assertError(result)(
        "You can use `foreachEntry` instead of `foreach` if Scala 2.13+"
      )
    )
  }
}
