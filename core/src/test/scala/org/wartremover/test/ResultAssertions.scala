package org.wartremover
package test

import org.scalatest.Assertions

trait ResultAssertions extends Assertions {

  def assertEmpty(result: WartTestTraverser.Result) = {
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

  def assertError(result: WartTestTraverser.Result)(message: String) = assertErrors(result)(message, 1)

  def assertErrors(result: WartTestTraverser.Result)(message: String, times: Int) = {
    assertResult(List.fill(times)(message), "result.errors")(result.errors.map(skipTraverserPrefix))
    assertResult(List.empty, "result.warnings")(result.warnings.map(skipTraverserPrefix))
  }

  def assertWarnings(result: WartTestTraverser.Result)(message: String, times: Int) = {
    assertResult(List.empty, "result.errors")(result.errors.map(skipTraverserPrefix))
    assertResult(List.fill(times)(message), "result.warnings")(result.warnings.map(skipTraverserPrefix))
  }

  private val messageFormat = """^\[wartremover:\S+\] (.+)$""".r

  private def skipTraverserPrefix(msg: String) = msg match {
    case messageFormat(rest) => rest
    case s => s
  }
}
