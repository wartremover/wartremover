package org.wartremover
package warts

object SortFilter extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "filter") =>
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).filter($p)
                  } =>
                error(t.pos, "Change order of `sorted` and `filter`")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).filter($p)
                  } =>
                error(t.pos, "Change order of `sortBy` and `filter`")
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sortWith($f: Function2[`t1`, `t1`, Boolean]).filter($p)
                  } =>
                error(t.pos, "Change order of `sortWith` and `filter`")

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
