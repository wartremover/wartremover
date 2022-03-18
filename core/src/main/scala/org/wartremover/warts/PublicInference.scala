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

    def isAcceptLiteralType(t: Type): Boolean = {
      t <:< typeOf[String] || t <:< typeOf[Char] || t <:< typeOf[Boolean]
    }

    def scoverage(tree: Tree): Boolean = {
      // https://github.com/wartremover/wartremover/issues/475
      // https://github.com/wartremover/wartremover/issues/532
      //
      // https://github.com/scoverage/scalac-scoverage-plugin/blob/9e1ea5c47f1a6cd/scalac-scoverage-runtime/shared/src/main/scala/scoverage/Invoker.scala#L36-L40
      // https://github.com/scoverage/scalac-scoverage-plugin/blob/c568ec50b01ccdf/scalac-scoverage-runtime/shared/src/main/scala/scoverage/Invoker.scala#L32
      // scoverage rewrite
      // val a = "b"
      // ↓
      // val a = {
      //   scoverage.Invoker.invoked(id: Int, dataDir: String)
      //   "b"
      // }
      tree match {
        case Block(Apply(q"scoverage.Invoker.invoked", Literal(_) :: Literal(_) :: Nil) :: Nil, Literal(c)) =>
          isAcceptLiteralType(c.tpe)
        case Block(Apply(q"scoverage.Invoker.invoked", Literal(_) :: Literal(_) :: Literal(_) :: Nil) :: Nil, Literal(c)) =>
          isAcceptLiteralType(c.tpe)
        case _ =>
          false
      }
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          // Ignore string, char and boolean literals
          case ValDef(_, _, _, Literal(c)) if isAcceptLiteralType(c.tpe) =>

          case ValDef(_, _, _, rhs) if scoverage(rhs) =>

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
