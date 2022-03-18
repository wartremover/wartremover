package org.wartremover
package test

import org.wartremover.warts.ExplicitImplicitTypes
import org.scalatest.funsuite.AnyFunSuite

class WartTraverserTest extends AnyFunSuite with ResultAssertions {
  test("isWartAnnotation should correctly look at the annotations of the accessed fields for accessors") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      class A {
        @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
        implicit val foo = "should be suppressed"
      }
    }
    assertEmpty(result)
  }

  test("hasTypeAscription should correctly look at the accessed fields for accessors") {
    val result = WartTestTraverser(ExplicitImplicitTypes) {
      class A {
        implicit var foo: String = "Should not be reported"
      }
    }
    assertEmpty(result)
  }
}
