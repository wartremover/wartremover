package org.wartremover
package warts

import scala.annotation.nowarn

object RedundantConversions extends WartTraverser {
  private val methodNames: Seq[String] = Seq(
    "toList",
    "toSeq",
    "toVector",
    "toStream",
    "toSet",
    "toIndexedSeq",
    "toString",
    "toInt",
    "toLong",
    "toFloat",
    "toDouble",
    "toByte",
    "toShort",
    "toChar",
  )

  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      @nowarn("msg=LazyList")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if methodNames.forall(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case s @ Select(t, method) =>
            method match {
              case "toList" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.List") =>
                error(selectNamePosition(s), "redundant toList conversion")
              case "toSeq" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.Seq") =>
                error(selectNamePosition(s), "redundant toSeq conversion")
              case "toVector" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.Vector") =>
                error(selectNamePosition(s), "redundant toVector conversion")
              case "toStream" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.Stream") =>
                error(selectNamePosition(s), "redundant toStream conversion")
              case "toSet" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.Set") =>
                error(selectNamePosition(s), "redundant toSet conversion")
              case "toIndexedSeq" if t.tpe.baseClasses.exists(_.fullName == "scala.collection.immutable.IndexedSeq") =>
                error(selectNamePosition(s), "redundant toIndexedSeq conversion")
              case "toString" if t.tpe <:< TypeRepr.of[String] =>
                error(selectNamePosition(s), "redundant toString conversion")
              case "toInt" if t.tpe <:< TypeRepr.of[Int] =>
                error(selectNamePosition(s), "redundant toInt conversion")
              case "toLong" if t.tpe <:< TypeRepr.of[Long] =>
                error(selectNamePosition(s), "redundant toLong conversion")
              case "toFloat" if t.tpe <:< TypeRepr.of[Float] =>
                error(selectNamePosition(s), "redundant toFloat conversion")
              case "toDouble" if t.tpe <:< TypeRepr.of[Double] =>
                error(selectNamePosition(s), "redundant toDouble conversion")
              case "toByte" if t.tpe <:< TypeRepr.of[Byte] =>
                error(selectNamePosition(s), "redundant toByte conversion")
              case "toShort" if t.tpe <:< TypeRepr.of[Short] =>
                error(selectNamePosition(s), "redundant toShort conversion")
              case "toChar" if t.tpe <:< TypeRepr.of[Char] =>
                error(selectNamePosition(s), "redundant toChar conversion")
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
