package org.wartremover
package warts

object SortedMaxMinOption extends WartTraverser {
  private val methodNames: Seq[String] = Seq(
    "sortBy",
    "sorted",
  )

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if getSourceCode(tree).fold(false)(src => !methodNames.exists(src.contains)) =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).headOption
                  } =>
                error(t.pos, "You can use minOption instead of sorted.headOption")
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).lastOption
                  } =>
                error(t.pos, "You can use maxOption instead of sorted.lastOption")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).headOption
                  } =>
                error(t.pos, "You can use minByOption instead of sortBy.headOption")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).lastOption
                  } =>
                error(t.pos, "You can use maxByOption instead of sortBy.lastOption")
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
