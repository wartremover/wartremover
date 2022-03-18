package org.wartremover
package warts

object DefaultArguments extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    import u.universe.Flag._

    object DefaultParam {
      def unapply(vparamss: List[List[ValDef]]): Option[ValDef] =
        vparamss.flatMap(_.find(_.mods.hasFlag(DEFAULTPARAM))).headOption
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case d@DefDef(_, _, _, DefaultParam(param), _, _) if !isSynthetic(u)(d) =>
            error(u)(param.pos, "Function has default arguments")
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
