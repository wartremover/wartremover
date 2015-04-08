package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.OptionPartial

class OptionPartialTest extends FunSuite {
  test("can't use Option#get on Some") {
    val result = WartTestTraverser(OptionPartial) {
      println(Some(1).get)
    }
    assertResult(List("Option#get is disabled - use Option#fold instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use Option#get on None") {
    val result = WartTestTraverser(OptionPartial) {
      println(None.get)
    }
    assertResult(List("Option#get is disabled - use Option#fold instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("doesn't detect other `get` methods") {
    val result = WartTestTraverser(OptionPartial) {
      case class A(get: Int)
      println(A(1).get)
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("OptionPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(OptionPartial) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.OptionPartial"))
      val foo = {
        println(Some(1).get)
        println(None.get)
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
