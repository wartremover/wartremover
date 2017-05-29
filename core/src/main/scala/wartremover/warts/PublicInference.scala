package org.wartremover
package warts

object PublicInference extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def isInPublicClass(t: Tree) = t.symbol.owner.isClass && t.symbol.owner.isPublic

    def isConstructorOrOverrides(t: Tree) = t.symbol.isMethod && (t.symbol.asMethod.isConstructor ||
        t.symbol.asMethod.allOverriddenSymbols.nonEmpty)

    def isAccessor(t: Tree) = t.symbol.isTerm && (t.symbol.asTerm.isParamAccessor || t.symbol.asTerm.isCaseAccessor)

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case t: ValOrDefDef if isPublic(u)(t) && isInPublicClass(t) &&
              !isConstructorOrOverrides(t) && !isAccessor(t) &&
              !hasTypeAscription(u)(t) && !isSynthetic(u)(tree) =>
            error(u)(tree.pos, "Public member must have an explicit type ascription")

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
