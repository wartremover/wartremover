package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.OptionPartial

class OptionPartialTest extends FunSuite with ResultAssertions {
  test("can't use Option#get on Some") {
    val result = WartTestTraverser(OptionPartial) {
      println(Some(1).get)
    }
    assertError(result)("[org.wartremover.warts.OptionPartial] Option#get is disabled - use Option#fold instead")
  }
  test("can't use Option#get on None") {
    val result = WartTestTraverser(OptionPartial) {
      println(None.get)
    }
    assertError(result)("[org.wartremover.warts.OptionPartial] Option#get is disabled - use Option#fold instead")
  }
  test("doesn't detect other `get` methods") {
    val result = WartTestTraverser(OptionPartial) {
      case class A(get: Int)
      println(A(1).get)
    }
    assertEmpty(result)
  }
  test("OptionPartial wart obeys SuppressWarnings") {
    val result = WartTestTraverser(OptionPartial) {
      @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
      val foo = {
        println(Some(1).get)
        println(None.get)
      }
    }
    assertEmpty(result)
  }
}
