package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ListAppend

class ListAppendTest2 extends AnyFunSuite with ResultAssertions {
  private def list: List[Int] = List.empty[Int]
  private def vector: Vector[Int] = Vector.empty[Int]

  test("Vector.appended") {
    val result = WartTestTraverser(ListAppend) {
      vector :+ 3
      vector `appended` 4
    }
    assertEmpty(result)
  }

  test("List.appended") {
    Seq(
      WartTestTraverser(ListAppend) {
        list.appended(3)
      },
      WartTestTraverser(ListAppend) {
        list `appended` 4
      }
    ).foreach { result =>
      assertError(result)(ListAppend.message)
    }
  }
}
