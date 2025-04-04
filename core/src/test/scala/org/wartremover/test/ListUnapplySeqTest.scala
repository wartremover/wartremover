package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ListUnapplySeq

class ListUnapplySeqTest extends AnyFunSuite with ResultAssertions {

  private[this] def collectionSeq: collection.Seq[Int] = ???
  private[this] def immutableSeq: collection.immutable.Seq[Int] = ???
  private[this] def collectionIterable: collection.Iterable[Int] = ???
  private[this] def immutableIterable: collection.immutable.Iterable[Int] = ???
  private[this] def list: List[Int] = ???
  private[this] def cons: ::[Int] = ???
  private[this] def any: Any = ???
  private[this] def anyRef: AnyRef = ???

  test("Any") {
    val result = WartTestTraverser(ListUnapplySeq) {
      val Seq(x1) = any
      val List(x2) = any
    }
    assertEmpty(result)
  }

  test("AnyRef") {
    val result = WartTestTraverser(ListUnapplySeq) {
      val Seq(x1) = anyRef
      val List(x2) = anyRef
    }
    assertEmpty(result)
  }

  test("val xs @ List(x1, x2) = notList") {
    val result = WartTestTraverser(ListUnapplySeq) {
      val xs @ List(x1, x2) = collectionSeq
    }
    assertError(result)(ListUnapplySeq.message)
  }

  test("bind val") {
    val result = WartTestTraverser(ListUnapplySeq) {
      val List(a1, a2) = collectionSeq
    }
    assertError(result)(ListUnapplySeq.message)
  }

  test("collection.Seq") {
    val result = WartTestTraverser(ListUnapplySeq) {
      collectionSeq match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapplySeq.message, 2)
  }

  test("immutable.Seq") {
    val result = WartTestTraverser(ListUnapplySeq) {
      immutableSeq match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapplySeq.message, 2)
  }

  test("collection.Iterable") {
    val result = WartTestTraverser(ListUnapplySeq) {
      collectionIterable match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapplySeq.message, 2)
  }

  test("immutable.Iterable") {
    val result = WartTestTraverser(ListUnapplySeq) {
      immutableIterable match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertErrors(result)(ListUnapplySeq.message, 2)
  }

  test("List") {
    val result = WartTestTraverser(ListUnapplySeq) {
      list match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertEmpty(result)
  }

  test("::") {
    val result = WartTestTraverser(ListUnapplySeq) {
      cons match {
        case List(_, _, _) =>
          0
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertEmpty(result)
  }

  test("type alias") {
    class Foo
    type MyListOfFoo = List[Foo]
    def x: MyListOfFoo = ???
    val result = WartTestTraverser(ListUnapplySeq) {
      x match {
        case List(_, _) =>
          1
        case _ =>
          2
      }
    }
    assertEmpty(result)
  }

  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ListUnapplySeq) {
      @SuppressWarnings(Array("org.wartremover.warts.ListUnapplySeq"))
      val foo = {
        immutableIterable match {
          case List(_, _, _) =>
            0
          case List(_, _) =>
            1
          case _ =>
            2
        }
      }
    }
    assertEmpty(result)
  }
}
