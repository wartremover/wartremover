package org.wartremover
package test

import org.wartremover.warts.Option2Iterable
import org.scalatest.funsuite.AnyFunSuite
import scala.annotation.nowarn

class Option2IterableTest extends AnyFunSuite with ResultAssertions {

  def isScala212: Boolean = {
    val v = scala.util.Properties.versionNumberString
    v.matches("2\\.1[012].*")
  }

  test("can't use Option.option2Iterable with Some") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Iterable(1).flatMap(Some(_)))
    }

    // https://github.com/scala/scala/pull/8038
    if (isScala212) {
      assertError(result)("Implicit conversion from Option to Iterable is disabled - use Option#toList instead")
    }
  }
  test("can't use Option.option2Iterable with None") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Iterable(1).flatMap(_ => None))
    }

    // https://github.com/scala/scala/pull/8038
    if (isScala212) {
      assertError(result)("Implicit conversion from Option to Iterable is disabled - use Option#toList instead")
    }
  }
  test("can't use Option.option2Iterable when zipping Options") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Option(1) zip Option(2))
    }
    if (isScala212) {
      assertErrors(result)("Implicit conversion from Option to Iterable is disabled - use Option#toList instead", 2)
    } else {
      // https://github.com/scala/scala/blob/v2.13.0-M4/src/library/scala/Option.scala#L321
      assertEmpty(result)
    }
  }
  test("doesn't detect user defined option2Iterable functions") {
    @nowarn("msg=method toIterable in trait Iterable is deprecated")
    def option2Iterable[A](o: Option[A]): Iterable[A] = o.toIterable
    val result = WartTestTraverser(Option2Iterable) {
      println(option2Iterable(Some(1)))
    }
    assertEmpty(result)
  }
  test("Option2Iterable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Option2Iterable) {
      @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
      val foo = {
        println(Iterable(1).flatMap(Some(_)))
      }
    }
    assertEmpty(result)
  }
}
