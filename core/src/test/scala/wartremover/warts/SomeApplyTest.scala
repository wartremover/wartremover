package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.SomeApply

class SomeApplyTest extends FunSuite with ResultAssertions {

  test("can't use Some.apply with null") {
    val result = WartTestTraverser(SomeApply) {
      Some(null)
    }
    assertError(result)("Some.apply is disabled - use Option.apply instead")
  }
  test("can't use Some.apply with a literal") {
    val result = WartTestTraverser(SomeApply) {
      Some(1)
    }
    assertError(result)("Some.apply is disabled - use Option.apply instead")
  }
  test("can't use Some.apply with an identifier") {
    val result = WartTestTraverser(SomeApply) {
      val x = 1
      Some(x)
    }
    assertError(result)("Some.apply is disabled - use Option.apply instead")
  }
  test("can use Some.unapply in pattern matching") {
    val result = WartTestTraverser(SomeApply) {
      Option("test") match {
        case Some(test) => println(test)
        case None => println("not gonna happen")
      }
    }
    assertEmpty(result)
  }
}
