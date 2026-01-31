package org.wartremover
package warts

object KeySet extends WartTraverser {
  private val sources: Seq[String] = Seq("_1", "map", "toSet")
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val mapSymbol = Symbol.requiredClass("scala.collection.Map")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sources.exists(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case Select(
                Apply(
                  TypeApply(s @ Select(obj, "map"), _ :: Nil),
                  List(
                    Block(
                      List(
                        DefDef(
                          _,
                          TermParamClause(ValDef(t1, _, _) :: Nil) :: Nil,
                          _,
                          Some(Select(Ident(t2), "_1"))
                        ),
                      ),
                      _: Closure
                    )
                  )
                ),
                "toSet"
              ) if (t1 == t2) && obj.tpe.derivesFrom(mapSymbol) =>
            error(selectNamePosition(s), "you can use keySet")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
