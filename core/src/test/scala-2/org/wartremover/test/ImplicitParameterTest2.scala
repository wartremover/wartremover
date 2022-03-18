package org.wartremover
package test

import org.wartremover.warts.ImplicitParameter
import org.scalatest.funsuite.AnyFunSuite

class ImplicitParameterTest2 extends AnyFunSuite with ResultAssertions {
  test("Parent context bounds are enabled") {
    val result = WartTestTraverser(ImplicitParameter) {
      class C[F] {
        def f[A](a: A)(implicit F: List[F]) = {}
      }
    }
    assertEmpty(result)
  }
}
