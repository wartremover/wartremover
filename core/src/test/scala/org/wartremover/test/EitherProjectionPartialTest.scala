package org.wartremover
package test


import org.wartremover.warts.EitherProjectionPartial
import org.scalatest.funsuite.AnyFunSuite

class EitherProjectionPartialTest extends AnyFunSuite with ResultAssertions {
  test("can't use LeftProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).left.get)
    }
    assertError(result)("LeftProjection#get is disabled - use LeftProjection#toOption instead")
  }
  test("can't use LeftProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).left.get)
    }
    assertError(result)("LeftProjection#get is disabled - use LeftProjection#toOption instead")
  }
  test("can't use RightProjection#get on Left") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Left(1).right.get)
    }
    assertError(result)("RightProjection#get is disabled - use RightProjection#toOption instead")
  }
  test("can't use RightProjection#get on Right") {
    val result = WartTestTraverser(EitherProjectionPartial) {
      println(Right(1).right.get)
    }
    assertError(result)("RightProjection#get is disabled - use RightProjection#toOption instead")
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
