package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ListAppend

class ListAppendTest extends AnyFunSuite with ResultAssertions {
  private[this] def collectionSeq: collection.Seq[Int] = Seq.empty[Int]
  private[this] def immutableSeq: collection.immutable.Seq[Int] = List.empty[Int]
  private[this] def list: List[Int] = List.empty[Int]
  private[this] def vector: Vector[Int] = Vector.empty[Int]

  test("collection.Seq") {
    val result = WartTestTraverser(ListAppend) {
      collectionSeq :+ 3
    }
    assertEmpty(result)
  }

  test("immutable.Seq") {
    val result = WartTestTraverser(ListAppend) {
      immutableSeq :+ 3
    }
    assertEmpty(result)
  }

  test("Vector") {
    val result = WartTestTraverser(ListAppend) {
      vector :+ 3
    }
    assertEmpty(result)
  }

  test("List") {
    val result = WartTestTraverser(ListAppend) {
      list :+ 3
    }
    assertError(result)(ListAppend.message)
  }

  test("ListAppend wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ListAppend) {
      @SuppressWarnings(Array("org.wartremover.warts.ListAppend"))
      val foo = list :+ 3
    }
    assertEmpty(result)
  }
}
