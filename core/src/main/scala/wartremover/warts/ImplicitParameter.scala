package org.wartremover
package warts

object ImplicitParameter extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def flatTypesFrom(t: Type): Set[Type] = Set(t) ++ t.typeArgs.flatMap(x => flatTypesFrom(x))

    def isImplicitParamTypeInTparams(param: ValDef, tparamSymbols: Set[Symbol]) =
      (flatTypesFrom(param.tpt.tpe) - param.tpt.tpe).exists { t =>
        tparamSymbols.contains(t.typeSymbol)
      }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case DefDef(_, _, tparams, paramss, _, _) if !isSynthetic(u)(tree) && {
            val tsymbols = tparams.map(_.symbol).toSet
            paramss.last.exists { x =>
              x.symbol.isImplicit && !x.symbol.isSynthetic && !isImplicitParamTypeInTparams(x, tsymbols)
            }
          } =>
            u.error(tree.pos, "Implicit parameters are disabled")

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
