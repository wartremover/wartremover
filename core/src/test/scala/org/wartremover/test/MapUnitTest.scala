package org.wartremover
package test

import org.wartremover.warts.MapUnit
import org.scalatest.funsuite.AnyFunSuite

class MapUnitTest extends AnyFunSuite with ResultAssertions {
  private def message = "Maybe you should use `foreach` instead of `map`"

  test("collection.map(return `Unit` function) disabled") {
    def xs: Seq[Int] = ???
    def f: Int => Unit = ???
    val result = WartTestTraverser(MapUnit) {
      xs.map(f)
    }
    assertError(result)(message)
  }

  test("non collction types allowed") {
    val result = WartTestTraverser(MapUnit) {
      Option(1).map(a => ())
      Right(1).map(a => ())
    }
    assertEmpty(result)
  }

  test("non Unit type functions allowed") {
    val result = WartTestTraverser(MapUnit) {
      List(2).map(a => a)
      Vector(2).map(_.toString)
    }
    assertEmpty(result)
  }

  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(MapUnit) {

      @SuppressWarnings(Array("org.wartremover.warts.MapUnit"))
      def x = List.empty[String].map(b => ())
    }
    assertEmpty(result)
  }
}
