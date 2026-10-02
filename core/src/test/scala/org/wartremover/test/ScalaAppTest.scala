package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.ScalaApp

class ScalaAppTest extends AnyFunSuite with ResultAssertions {
  test("scala.App is disabled") {
    val result = WartTestTraverser(ScalaApp) {
      object MyMain extends App
    }
    assertError(result)("Don't use scala.App. https://docs.scala-lang.org/scala3/book/methods-main-methods.html")
  }

  test("scala.App wart obeys SuppressWarnings") {
    val result = WartTestTraverser(ScalaApp) {
      @SuppressWarnings(Array("org.wartremover.warts.ScalaApp"))
      object MyMain extends App
    }
    assertEmpty(result)
  }
}
