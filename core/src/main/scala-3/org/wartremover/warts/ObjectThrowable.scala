package org.wartremover
package warts

object ObjectThrowable extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if sourceCodeNotContains(tree, "object") && sourceCodeNotContains(tree, "case") =>
          case t if hasWartAnnotation(t) =>
          case t: ClassDef if t.symbol.flags.is(Flags.Module) =>
            val types = t.parents.collect {
              case a: Term =>
                a.tpe
              case a: TypeTree =>
                a.tpe
            }
            if (
              types.exists(_ <:< TypeRepr.of[java.lang.Throwable]) && types
                .forall(a => !(a <:< TypeRepr.of[scala.util.control.NoStackTrace]))
            ) {
              error(t.pos, "use class if extends Throwable")
            }
            super.traverseTree(tree)(owner)
          case t: ValDef
              if t.symbol.flags.is(Flags.Case) &&
                t.symbol.flags.is(Flags.Enum) &&
                (t.tpt.tpe <:< TypeRepr.of[java.lang.Throwable]) &&
                !(t.tpt.tpe <:< TypeRepr.of[scala.util.control.NoStackTrace]) =>
            error(t.pos, "Don't use empty param enum case if extends Throwable")
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
