package org.wartremover
package warts

object ForeachEntry extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t if t.isExpr =>
            t.asExpr match {
              case '{ ($x: collection.Map[t1, t2]).foreach($f) } =>
                f.asTerm match {
                  case b @ Block(
                        List(
                          DefDef(
                            _,
                            _,
                            _,
                            Some(
                              Match(
                                _,
                                cases
                              )
                            )
                          )
                        ),
                        _: Closure
                      ) =>
                    if (
                      cases.nonEmpty && cases.forall {
                        case CaseDef(Unapply(TypeApply(Select(Ident("Tuple2"), "unapply"), _), _, _), _, _) =>
                          true
                        case _ =>
                          false
                      }
                    ) {
                      error(tree.pos, "You can use `foreachEntry` instead of `foreach` if Scala 2.13+")
                    }
                  case _ =>
                }
              case _ =>
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
