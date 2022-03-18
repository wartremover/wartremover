package org.wartremover
package warts

object IterableOps extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        def err(method: String, alternative: String) =
          error(tree.pos, s"${method} is disabled - use ${alternative} instead")

        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Iterable[t]).head } =>
                err("head", "headOption")
              case '{ ($x: collection.Iterable[t]).tail } =>
                err("tail", "drop(1)")
              case '{ ($x: collection.Iterable[t]).init } =>
                err("init", "dropRight(1)")
              case '{ ($x: collection.Iterable[t]).last } =>
                err("last", "lastOption")
              case '{ ($x: collection.Iterable[t]).reduce($f) } =>
                err("reduce", "reduceOption or fold")
              case '{ ($x: collection.Iterable[t]).reduceLeft($f) } =>
                err("reduceLeft", "reduceLeftOption or foldLeft")
              case '{ ($x: collection.Iterable[t]).reduceRight($f) } =>
                err("reduceRight", "reduceRightOption or foldRight")
              case '{
                    type t1
                    type t2
                    ($x: collection.Iterable[`t1`]).maxBy($f: Function[`t1`, `t2`])($o: Ordering[`t2`])
                  } =>
                err("maxBy", "foldLeft or foldRight")
              case '{ ($x: collection.Iterable[t]).max($o) } =>
                err("max", "foldLeft or foldRight")
              case '{
                    type t1
                    type t2
                    ($x: collection.Iterable[`t1`]).minBy($f: Function[`t1`, `t2`])($o: Ordering[`t2`])
                  } =>
                err("minBy", "foldLeft or foldRight")
              case '{ ($x: collection.Iterable[t]).min($o) } =>
                err("min", "foldLeft or foldRight")
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
