package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ListUnapply

class ListUnapplyTest extends AnyFunSuite with ResultAssertions {

  private[this] val a = Vector(1, 2)
  private[this] def collectionSeq: collection.Seq[Int] = a
  private[this] def immutableSeq: collection.immutable.Seq[Int] = a
  private[this] def collectionIterable: collection.Iterable[Int] = a
  private[this] def immutableIterable: collection.immutable.Iterable[Int] = a
  private[this] val list: List[Int] = List(1, 2)
  private[this] val cons: ::[Int] = ::(1, Nil)

  test("collection.Seq") {
    val result = WartTestTraverser(ListUnapply) {
      collectionSeq match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapply.message, 2)
  }

  test("immutable.Seq") {
    val result = WartTestTraverser(ListUnapply) {
      immutableSeq match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapply.message, 2)
  }

  test("collection.Iterable") {
    val result = WartTestTraverser(ListUnapply) {
      collectionIterable match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapply.message, 2)
  }

  test("immutable.Iterable") {
    val result = WartTestTraverser(ListUnapply) {
      immutableIterable match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapply.message, 2)
  }

  test("List") {
    val result = WartTestTraverser(ListUnapply) {
      list match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertEmpty(result)
  }

  test("::") {
    val result = WartTestTraverser(ListUnapply) {
      cons match {
        case _ :: _ :: _ =>
          0
        case _ :: _ =>
          1
        case _ =>
          2
      }
    }
    assertEmpty(result)
  }

  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ListUnapply) {
      @SuppressWarnings(Array("org.wartremover.warts.ListUnapply"))
      val foo = {
        immutableIterable match {
          case _ :: _ :: _ =>
            0
          case _ :: _ =>
            1
          case _ =>
            2
        }
      }
    }
    assertEmpty(result)
  }
}
