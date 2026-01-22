package org.wartremover
package test

import org.wartremover.warts.ForeachEntry
import org.scalatest.funsuite.AnyFunSuite

class ForeachEntryTest2 extends AnyFunSuite with ResultAssertions {
  private def collectionMap: collection.Map[Int, String] = ???

  test("report error") {
    val result = WartTestTraverser(ForeachEntry) {
      // TODO Scala 2.13
      for ((k, v) <- collectionMap) {
        println(k)
        println(v)
      }
    }

    assertError(result.copy(errors = result.errors.distinct))(
      "You can use `foreachEntry` instead of `foreach` if Scala 2.13+"
    )
  }
}
