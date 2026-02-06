package org.wartremover
package warts

object SortedMaxMin extends WartTraverser {
  private val sortMethodNames: Seq[String] = Seq(
    "sortBy",
    "sorted",
  )
  private val headOrLast: Seq[String] = Seq(
    "head",
    "last",
  )

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _
              if sortMethodNames
                .forall(sourceCodeNotContains(tree, _)) || headOrLast.forall(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case t: Select if t.isExpr =>
            t.asExpr match {
              case '{
                    type t1
                    ($x: collection.Seq[`t1`]).sorted(using $o: Ordering[`t1`]).head
                  } =>
                error(selectNamePosition(t), "You can use min instead of sorted.head")
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
                error(selectNamePosition(t), "You can use minBy instead of sortBy.head")
              case '{
                    type t1
                    type t2
                    ($x: collection.Seq[`t1`]).sortBy($f: Function1[`t1`, `t2`])(using $o: Ordering[`t2`]).last
                  } =>
                error(selectNamePosition(t), "You can use maxBy instead of sortBy.last")
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
