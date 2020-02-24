package org.wartremover
package warts

object PublicInference extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def isInPublicClass(t: Tree) = t.symbol.owner.isClass && t.symbol.owner.isPublic

    def isConstructorOrOverrides(t: Tree) =
      if (t.symbol.isMethod)
        t.symbol.asMethod.isConstructor || t.symbol.asMethod.overrides.nonEmpty
      else if (t.symbol.isTerm && t.symbol.asTerm.getter.isMethod && (t.symbol.owner.isType || t.symbol.owner.isModule))
        t.symbol.asTerm.getter.asMethod.overrides.nonEmpty
      else
        false

    def isAccessor(t: Tree) = t.symbol.isTerm && (t.symbol.asTerm.isParamAccessor || t.symbol.asTerm.isCaseAccessor)

    def isMacroExpansion(t: ValOrDefDef) = {
      val pos = t.pos.start
      pos == t.pos.end && pos == t.symbol.owner.pos.start && pos == t.symbol.owner.pos.end
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          // Ignore string, char and boolean literals
          case ValDef(_, _, _, Literal(c))
            if c.tpe <:< typeOf[String] || c.tpe <:< typeOf[Char] || c.tpe <:< typeOf[Boolean] =>

          case t: ValOrDefDef if isPublic(u)(t) && isInPublicClass(t) &&
              !isConstructorOrOverrides(t) && !isAccessor(t) &&
              !hasTypeAscription(u)(t) && !isSynthetic(u)(tree) && !isMacroExpansion(t) =>
            error(u)(tree.pos, "Public member must have an explicit type ascription")

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
