package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite
import org.brianmckenna.wartremover.warts.TryPartial
import scala.util.{Try, Success, Failure}

class TryPartialTest extends FunSuite {
  test("can't use Try#get on Success") {
    val result = WartTestTraverser(TryPartial) {
      println(Success(1).get)
    }
    assertResult(List("Try#get is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use Try#get on Failure") {
    val result = WartTestTraverser(TryPartial) {
      println(Failure(new Error).get)
    }
    assertResult(List("Try#get is disabled"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("doesn't detect other `get` methods") {
    val result = WartTestTraverser(TryPartial) {
      case class A(get: Int)
      println(A(1).get)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("TryPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(TryPartial) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.TryPartial"))
      val foo = {
        println(Success(1).get)
        println(Failure(new Error).get)
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
