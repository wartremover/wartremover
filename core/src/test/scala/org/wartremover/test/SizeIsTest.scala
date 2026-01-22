package org.wartremover
package test

import org.wartremover.warts.SizeIs
import org.scalatest.funsuite.AnyFunSuite

class SizeIsTest extends AnyFunSuite with ResultAssertions {
  test("suggest sizeIs") {
    Seq(
      WartTestTraverser(SizeIs) {
        List(3).size == 1
      },
      WartTestTraverser(SizeIs) {
        Vector(3).size <= 1
      },
      WartTestTraverser(SizeIs) {
        Iterable(3).size >= 1
      },
      WartTestTraverser(SizeIs) {
        Seq(3).size != 1
      },
      WartTestTraverser(SizeIs) {
        Map.empty[Int, String].size > 1
      },
      WartTestTraverser(SizeIs) {
        Set.empty[Boolean].size < 1
      }
    ).foreach { result =>
      assertError(result)("Maybe you can use `sizeIs` instead of `size`")
    }
  }

  test("suggest lengthIs") {
    Seq(
      WartTestTraverser(SizeIs) {
        List(3).length == 1
      },
      WartTestTraverser(SizeIs) {
        Vector(3).length <= 1
      },
      WartTestTraverser(SizeIs) {
        IndexedSeq(1).length >= 1
      }
    ).foreach { result =>
      assertError(result)("Maybe you can use `lengthIs` instead of `length`")
    }
  }

  test("don't suggest if Long") {
    def long: Long = 7
    val result = WartTestTraverser(SizeIs) {
      List(3).length == 5L
      Vector(3).length <= 6L
      IndexedSeq(1).length >= long
    }
    assertEmpty(result)
  }

  test("don't suggest if not scala collection types") {
    object MyObj {
      def length: Int = 3
      def size: Int = 3
    }
    val result = WartTestTraverser(SizeIs) {
      Array(2).size == 1
      Array(true).length <= 3
      "foo".size == 2
      "foo".length == 3
      (null: java.util.List[String]).size > 4
      MyObj.size == 2
      MyObj.length == 2
    }
    assertEmpty(result)
  }

  test("don't suggest if not compare methods") {
    val result = WartTestTraverser(SizeIs) {
      List(3).size.toString
      Vector(3).length + 2
      Iterable(3).size - 1
      Map.empty[Int, String].size * 2
      Set.empty[Boolean].size to 3
    }
    assertEmpty(result)
  }
}
