package org.wartremover
package warts

object SortedMaxMin extends WartTraverser {
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
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).head
                  } =>
                error(t.pos, "You can use min instead of sorted.head")
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).last
                  } =>
                error(t.pos, "You can use max instead of sorted.last")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).head
                  } =>
                error(t.pos, "You can use minBy instead of sortBy.head")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).last
                  } =>
                error(t.pos, "You can use maxBy instead of sortBy.last")
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
