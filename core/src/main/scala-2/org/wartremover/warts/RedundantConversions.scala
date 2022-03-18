package org.wartremover
package warts

import scala.annotation.nowarn

object RedundantConversions extends WartTraverser {

  @nowarn("msg=Stream")
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val values: Map[Name, (String, Type)] = Seq(
      "toList" -> typeOf[List[Any]],
      "toSeq" -> typeOf[collection.immutable.Seq[Any]],
      "toVector" -> typeOf[Vector[Any]],
      "toSet" -> typeOf[Set[Any]],
      "toStream" -> typeOf[Stream[Any]],
      "toIndexedSeq" -> typeOf[collection.immutable.IndexedSeq[Any]]
    ).map { case (name, tpe) =>
      TermName(name) -> Tuple2(name, tpe)
    }.toMap

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Select(obj, TermName("toString")) if obj.tpe <:< typeOf[String] =>
            error(u)(tree.pos, "redundant toString conversion")
          case Select(obj, method) =>
            values.get(method) match {
              case Some((name, tpe)) if obj.tpe.typeConstructor <:< tpe.typeConstructor =>
                error(u)(tree.pos, s"redundant ${name} conversion")
              case _ =>
                super.traverse(tree)
            }
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
