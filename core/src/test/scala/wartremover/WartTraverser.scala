package org.wartremover
package test

import org.scalatest.FunSuite

import org.wartremover.warts.ExplicitImplicitTypes

class WartTraverserTest extends FunSuite with ResultAssertions {
  test("isWartAnnotation should correctly look at the annotations of the accessed fields for accessors") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      class A {
        @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
        implicit val foo = "should be suppressed"
      }
    }
    assertEmpty(result)
  }
}
