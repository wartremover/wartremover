package org.brianmckenna.wartremover
package test

import org.scalatest.FunSuite

import org.brianmckenna.wartremover.warts.Option2Iterable

class Option2IterableTest extends FunSuite {

  test("can't use Option.option2Iterable with Some") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Iterable(1).flatMap(Some(_)))
    }
    assertResult(List("Implicit conversion from Option to Iterable is disabled - use Option#toList instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use Option.option2Iterable with None") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Iterable(1).flatMap(_ => None))
    }
    assertResult(List("Implicit conversion from Option to Iterable is disabled - use Option#toList instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("can't use Option.option2Iterable when zipping Options") {
    val result = WartTestTraverser(Option2Iterable) {
      println(Option(1) zip Option(2))
    }
    assertResult(List("Implicit conversion from Option to Iterable is disabled - use Option#toList instead", "Implicit conversion from Option to Iterable is disabled - use Option#toList instead"), "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("doesn't detect user defined option2Iterable functions") {
    def option2Iterable[A](o: Option[A]): Iterable[A] = o.toIterable
    val result = WartTestTraverser(Option2Iterable) {
      println(option2Iterable(Some(1)))
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
  test("Option2Iterable wart obeys SuppressWarnings") {
    val result = WartTestTraverser(Option2Iterable) {
      @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Option2Iterable"))
      val foo = {
        println(Iterable(1).flatMap(Some(_)))
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }
}
