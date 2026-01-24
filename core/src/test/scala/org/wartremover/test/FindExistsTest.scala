package org.wartremover
package test

import org.wartremover.warts.FindExists
import org.scalatest.funsuite.AnyFunSuite

class FindExistsTest extends AnyFunSuite with ResultAssertions {
  private def seq: Seq[Int] = Nil

  test("find and isEmpty") {
    val result = WartTestTraverser(FindExists) {
      seq.find(_ > 0).isEmpty
    }
    assertError(result)("you can use exists instead of find and isEmpty")
  }

  test("find and nonEmpty") {
    val result = WartTestTraverser(FindExists) {
      seq.find(_ > 0).nonEmpty
    }
    assertError(result)("you can use exists instead of find and nonEmpty")
  }

  test("find and isDefined") {
    val result = WartTestTraverser(FindExists) {
      seq.find(_ > 0).isDefined
    }
    assertError(result)("you can use exists instead of find and isDefined")
  }

  test("not Iterable") {
    trait MyClass {
      def find(f: Int => Boolean): Option[Int]
    }
    val result = WartTestTraverser(FindExists) {
      def f(x: MyClass) = Seq(
        x.find(_ > 0).isDefined,
        x.find(_ > 0).isEmpty,
        x.find(_ > 0).nonEmpty,
      )
    }
    assertEmpty(result)
  }

  test("SuppressWarnings") {
    val result = WartTestTraverser(FindExists) {
      @SuppressWarnings(Array("org.wartremover.warts.FindExists"))
      class Y {
        def f1 = seq.find(_ > 0).isDefined
        def f2 = seq.find(_ > 0).isEmpty
        def f3 = seq.find(_ > 0).nonEmpty
      }
    }
    assertEmpty(result)
  }
}
