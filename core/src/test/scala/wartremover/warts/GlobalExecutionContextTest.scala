package org.wartremover
package test

import org.scalatest.funsuite.AnyFunSuite
import org.wartremover.warts.GlobalExecutionContext
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class GlobalExecutionContextTest extends AnyFunSuite with ResultAssertions {
  test("report error if use ExecutionContext.global") {
    val result = WartTestTraverser(GlobalExecutionContext) {
      ExecutionContext.Implicits.global
      Future(3)(ExecutionContext.global)
    }
    assertErrors(result)(GlobalExecutionContext.message, 2)
  }
  test("wart obeys SuppressWarnings") {
    val result = WartTestTraverser(GlobalExecutionContext) {
      @SuppressWarnings(Array("org.wartremover.warts.GlobalExecutionContext"))
      val foo: Unit = {
        ExecutionContext.Implicits.global
        Future(3)(ExecutionContext.global)
      }
    }
    assertEmpty(result)
  }
}
