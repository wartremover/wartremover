package org.wartremover
package test

import org.wartremover.warts.TryPartial
import scala.util.{Try, Success, Failure}
import org.scalatest.funsuite.AnyFunSuite

class TryPartialTest extends AnyFunSuite with ResultAssertions {
  test("can't use Try#get on Success") {
    val result = WartTestTraverser(TryPartial) {
      println(Success(1).get)
    }
    assertError(result)("Try#get is disabled")
  }
  test("can't use Try#get on Failure") {
    val result = WartTestTraverser(TryPartial) {
      println(Failure(new Error).get)
    }
    assertError(result)("Try#get is disabled")
  }
  test("doesn't detect other `get` methods") {
    val result = WartTestTraverser(TryPartial) {
      case class A(get: Int)
      println(A(1).get)
    }
    assertEmpty(result)
  }
  test("TryPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(TryPartial) {
      @SuppressWarnings(Array("org.wartremover.warts.TryPartial"))
      val foo = {
        println(Success(1).get)
        println(Failure(new Error).get)
      }
    }
    assertEmpty(result)
  }
}
