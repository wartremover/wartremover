package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ExplicitImplicitTypes

class WartTraverserTest extends FunSuite with ResultAssertions {
  test("hasTypeAscription should correctly look at the accessed fields for accessors") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      class A {
        implicit var foo: String = "Should not be reported"
      }
    }
    assertEmpty(result)
  }
}
