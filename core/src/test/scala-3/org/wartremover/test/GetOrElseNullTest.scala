package org.wartremover
package test

import org.wartremover.warts.GetOrElseNull
import org.scalatest.funsuite.AnyFunSuite

class GetOrElseNullTest extends AnyFunSuite with ResultAssertions {
  test("getOrElse(null) disabled") {
    val result = WartTestTraverser(GetOrElseNull) {
      Option("x").getOrElse(null)
    }
    assertError(result)("you can use orNull instead of getOrElse(null)")
  }

  test("GetOrElseNull wart obeys SuppressWarnings") {
    val result = WartTestTraverser(GetOrElseNull) {
      @SuppressWarnings(Array("org.wartremover.warts.GetOrElseNull"))
      def y = Option("x").getOrElse(null)
    }
    assertEmpty(result)
  }
}
