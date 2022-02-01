package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.Contains

class ContainsTest extends AnyFunSuite with ResultAssertions {

  val msg = "Don't use List or Option `contains` method because is not typesafe"

  test("List.contains") {
    val result = WartTestTraverser(Contains) {
      List(1).contains(1)
    }

    assertError(result)(msg)
  }

  test("Option.contains") {
    val result = WartTestTraverser(Contains) {
      Option(1).contains("1")
    }

    assertError(result)(msg)
  }

  test("List.flatten.contains") {
    def l = List(List(1, 2, 3), List(4, 5, 6, 7))

    val result = WartTestTraverser(Contains) {
      l.flatten.contains(1)
    }

    assertError(result)(msg)
  }

  test("Contains wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Contains) {
      @SuppressWarnings(Array("org.wartremover.warts.Contains"))
      val foo = List(1).contains(1)
    }

    assertEmpty(result)
  }
}
