package org.wartremover
package test

import org.wartremover.warts.JavaConversions
import org.scalatest.funsuite.AnyFunSuite

class JavaConversionsTest extends AnyFunSuite with ResultAssertions {
  test("handle explicit method reference") {
    val result = WartTestTraverser(JavaConversions) {
      def ff[A](it: Iterable[A]) = collection.JavaConversions.asJavaCollection(it)
    }
    assertError(result)("scala.collection.JavaConversions is disabled - use scala.collection.JavaConverters instead")
  }
  test("disable scala.collection.JavaConversions when referenced in an import") {
    val result = WartTestTraverser(JavaConversions) {
      import scala.collection.JavaConversions._
      val x: java.util.List[Int] = new java.util.ArrayList[Int]
      val y: Seq[Int] = x
    }
    assertError(result)("scala.collection.JavaConversions is disabled - use scala.collection.JavaConverters instead")
  }
  test("JavaConversions wart obeys SuppressWarnings") {
    val result = WartTestTraverser(JavaConversions) {
      @SuppressWarnings(Array("org.wartremover.warts.JavaConversions"))
      def ff[A](it: Iterable[A]) = collection.JavaConversions.asJavaCollection(it)
    }
    assertEmpty(result)
  }
}
