package org.wartremover
package warts

object ForeachEntry extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val mapTypeConstructor = rootMirror.staticClass("scala.collection.Map").toTypeConstructor

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>

          case Apply(
                TypeApply(x @ Select(obj, TermName("foreach")), _),
                List(
                  Function(
                    List(generatedParam),
                    Match(
                      _,
                      cases
                    )
                  )
                )
              )
              if obj.tpe.typeConstructor <:< mapTypeConstructor && generatedParam.mods.hasFlag(
                Flag.SYNTHETIC
              ) && cases.nonEmpty && cases.forall {
                case CaseDef(
                      Apply(_, List(_, _)),
                      _,
                      _
                    ) =>
                  true
                case _ =>
                  false
              } =>
            error(u)(x.pos, "You can use `foreachEntry` instead of `foreach` if Scala 2.13+")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
