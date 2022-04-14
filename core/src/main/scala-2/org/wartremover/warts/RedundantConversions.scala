package org.wartremover
package warts

object RedundantConversions extends WartTraverser {

  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val anyValValues: Map[Name, (String, Type)] = Seq(
      "toInt" -> typeOf[Int],
      "toLong" -> typeOf[Long],
      "toFloat" -> typeOf[Float],
      "toDouble" -> typeOf[Double],
      "toByte" -> typeOf[Byte],
      "toShort" -> typeOf[Short],
      "toChar" -> typeOf[Char]
    ).map { case (name, tpe) =>
      TermName(name) -> Tuple2(name, tpe)
    }.toMap
    val collectionValues: Map[Name, (String, Type)] = Seq(
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
            collectionValues.get(method) match {
              case Some((name, tpe)) if obj.tpe.typeConstructor <:< tpe.typeConstructor =>
                error(u)(tree.pos, s"redundant ${name} conversion")
              case _ =>
                anyValValues.get(method) match {
                  case Some((name, tpe)) if obj.tpe =:= tpe =>
                    error(u)(tree.pos, s"redundant ${name} conversion")
                  case _ =>
                    super.traverse(tree)
                }
            }
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
