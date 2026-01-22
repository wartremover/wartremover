package org.wartremover
package warts

object ForeachEntry extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "for") =>
          case t if hasWartAnnotation(t) =>
          case Apply(
                TypeApply(Select(x, "foreach"), _),
                Block(
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
                ) :: Nil
              ) if cases.nonEmpty && cases.forall {
                case CaseDef(Unapply(TypeApply(Select(Ident("Tuple2"), "unapply"), _), _, _), _, _) =>
                  true
                case _ =>
                  false
              } && x.tpe.baseClasses.exists(_.fullName == "scala.collection.Map") =>
            error(tree.pos, "You can use `foreachEntry` instead of `foreach` if Scala 2.13+")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
