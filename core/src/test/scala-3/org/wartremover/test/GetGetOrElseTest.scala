package org.wartremover
package test

import org.wartremover.warts.GetGetOrElse
import org.scalatest.funsuite.AnyFunSuite

class GetGetOrElseTest extends AnyFunSuite with ResultAssertions {
  test("get.getOrElse disabled") {
    val result = WartTestTraverser(GetGetOrElse) {
      Map.empty[Int, String].get(3).getOrElse("p")
    }
    assertError(result)("you can use Map#getOrElse(key, default) instead of get(key).getOrElse(default)")
  }

  test("GetGetOrElse wart obeys SuppressWarnings") {
    val result = WartTestTraverser(GetGetOrElse) {
      @SuppressWarnings(Array("org.wartremover.warts.GetGetOrElse"))
      def y = Map.empty[Int, String].get(3).getOrElse("p")
    }
    assertEmpty(result)
  }
}
