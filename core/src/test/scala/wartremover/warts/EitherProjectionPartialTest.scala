package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.EitherProjectionPartial

class EitherProjectionPartialTest extends FunSuite with ResultAssertions {
  test("can't use LeftProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).left.get)
    }
    assertError(result)("[org.wartremover.warts.EitherProjectionPartial] LeftProjection#get is disabled - use LeftProjection#toOption instead")
  }
  test("can't use LeftProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).left.get)
    }
    assertError(result)("[org.wartremover.warts.EitherProjectionPartial] LeftProjection#get is disabled - use LeftProjection#toOption instead")
  }
  test("can't use RightProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).right.get)
    }
    assertError(result)("[org.wartremover.warts.EitherProjectionPartial] RightProjection#get is disabled - use RightProjection#toOption instead")
  }
  test("can't use RightProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).right.get)
    }
    assertError(result)("[org.wartremover.warts.EitherProjectionPartial] RightProjection#get is disabled - use RightProjection#toOption instead")
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
    assertEmpty(result)
  }
}
