package org.wartremover
package test

import org.scalatest.FunSuite
import org.wartremover.warts.TraversableOps

class TraversableOpsTest extends FunSuite {
  Seq[(String, Traversable[Any])]("List" -> List(1), "Seq" -> Seq(1), "Map" -> Map(1 -> 1)).foreach { case (name, x) =>
    test(s"can't use $name#head") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.head)
      }
      assertResult(List("head is disabled - use headOption instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#tail") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.tail)
      }
      assertResult(List("tail is disabled - use drop(1) instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#init") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.init)
      }
      assertResult(List("init is disabled - use dropRight(1) instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#last") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.last)
      }
      assertResult(List("last is disabled - use lastOption instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#reduce") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduce(_.hashCode + _.hashCode))
      }
      assertResult(List("reduce is disabled - use reduceOption or fold instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#reduceLeft") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduceLeft(_.hashCode + _.hashCode))
      }
      assertResult(List("reduceLeft is disabled - use reduceLeftOption or foldLeft instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }

    test(s"can't use $name#reduceRight") {
      val result = WartTestTraverser(TraversableOps) {
        println(x.reduceRight(_.hashCode + _.hashCode))
      }
      assertResult(List("reduceRight is disabled - use reduceRightOption or foldRight instead"), "result.errors")(result.errors)
      assertResult(List.empty, "result.warnings")(result.warnings)
    }
  }

  test("TraversableOps wart obeys SuppressWarnings") {
    val result = WartTestTraverser(TraversableOps) {
      @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
      val foo = {
        println(List(1).head)
        println(List().tail)
        println(List().init)
        println(List().last)
        println(List.empty[Int].reduce(_ + _))
        println(List.empty[Int].reduceLeft(_ + _))
        println(List.empty[Int].reduceRight(_ + _))
      }
    }
    assertResult(List.empty, "result.errors")(result.errors)
    assertResult(List.empty, "result.warnings")(result.warnings)
  }

}
