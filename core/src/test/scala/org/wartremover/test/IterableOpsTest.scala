package org.wartremover
package test

import org.wartremover.warts.IterableOps
import org.scalatest.funsuite.AnyFunSuite

class IterableOpsTest extends AnyFunSuite with ResultAssertions {

  implicit val ordering: Ordering[Any] = new Ordering[Any] {
    override def compare(x: Any, y: Any) = 0
  }

  Seq[(String, Iterable[Any])]("List" -> List(1), "Seq" -> Seq(1), "Map" -> Map(1 -> 1)).foreach { case (name, x) =>
    test(s"can't use $name#head") {
      val result = WartTestTraverser(IterableOps) {
        println(x.head)
      }
      assertError(result)("head is disabled - use headOption instead")
    }

    test(s"can't use $name#tail") {
      val result = WartTestTraverser(IterableOps) {
        println(x.tail)
      }
      assertError(result)("tail is disabled - use drop(1) instead")
    }

    test(s"can't use $name#init") {
      val result = WartTestTraverser(IterableOps) {
        println(x.init)
      }
      assertError(result)("init is disabled - use dropRight(1) instead")
    }

    test(s"can't use $name#last") {
      val result = WartTestTraverser(IterableOps) {
        println(x.last)
      }
      assertError(result)("last is disabled - use lastOption instead")
    }

    test(s"can't use $name#reduce") {
      val result = WartTestTraverser(IterableOps) {
        println(x.reduce(_.hashCode + _.hashCode))
      }
      assertError(result)("reduce is disabled - use reduceOption or fold instead")
    }

    test(s"can't use $name#reduceLeft") {
      val result = WartTestTraverser(IterableOps) {
        println(x.reduceLeft(_.hashCode + _.hashCode))
      }
      assertError(result)("reduceLeft is disabled - use reduceLeftOption or foldLeft instead")
    }

    test(s"can't use $name#reduceRight") {
      val result = WartTestTraverser(IterableOps) {
        println(x.reduceRight(_.hashCode + _.hashCode))
      }
      assertError(result)("reduceRight is disabled - use reduceRightOption or foldRight instead")
    }

    test(s"can't use $name#max") {
      val result = WartTestTraverser(IterableOps) {
        println(x.max)
      }
      assertError(result)("max is disabled - use foldLeft or foldRight instead")
    }

    test(s"can't use $name#maxBy") {
      val result = WartTestTraverser(IterableOps) {
        println(x.maxBy(_.hashCode))
      }
      assertError(result)("maxBy is disabled - use foldLeft or foldRight instead")
    }

    test(s"can't use $name#min") {
      val result = WartTestTraverser(IterableOps) {
        println(x.min)
      }
      assertError(result)("min is disabled - use foldLeft or foldRight instead")
    }

    test(s"can't use $name#minBy") {
      val result = WartTestTraverser(IterableOps) {
        println(x.minBy(_.hashCode))
      }
      assertError(result)("minBy is disabled - use foldLeft or foldRight instead")
    }
  }

  test("IterableOps wart obeys SuppressWarnings") {
    val result = WartTestTraverser(IterableOps) {
      @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
      val foo = {
        println(List(1).head)
        println(List().tail)
        println(List().init)
        println(List().last)
        println(List.empty[Int].reduce(_ + _))
        println(List.empty[Int].reduceLeft(_ + _))
        println(List.empty[Int].reduceRight(_ + _))
        println(List.empty[Int].max)
        println(List.empty[Int].maxBy(identity))
        println(List.empty[Int].min)
        println(List.empty[Int].minBy(identity))
      }
    }
    assertEmpty(result)
  }

}
