package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.EitherProjectionPartial

class EitherProjectionPartialTest extends FunSuite {
  test("can't use LeftProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).left.get)
    }
    assertResult(List("LeftProjection#get is disabled - use LeftProjection#toOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use LeftProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).left.get)
    }
    assertResult(List("LeftProjection#get is disabled - use LeftProjection#toOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use RightProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).right.get)
    }
    assertResult(List("RightProjection#get is disabled - use RightProjection#toOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use RightProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).right.get)
    }
    assertResult(List("RightProjection#get is disabled - use RightProjection#toOption instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("EitherProjectionPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      @SuppressWarnings(Array("org.wartremover.warts.EitherProjectionPartial"))
      val foo = {
        println(Left(1).left.get)
        println(Right(1).left.get)
        println(Left(1).right.get)
        println(Right(1).right.get)
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
