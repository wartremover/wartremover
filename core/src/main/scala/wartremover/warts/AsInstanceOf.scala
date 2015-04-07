package org.brianmckenna.wartremover
package warts

object AsInstanceOf extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    val EqualsName: TermName = "equals"
    val AsInstanceOfName: TermName = "asInstanceOf"

    val allowedCasts = List(
      "scala.tools.nsc.interpreter.IMain" // REPL needs this
    ) // cannot do `map rootMirror.staticClass` here because then:
      //   scala.ScalaReflectionException: object scala.tools.nsc.interpreter.IMain in compiler mirror not found.

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        val synthetic = isSynthetic(u)(tree)
        tree match {

          // Ignore trees marked by ignoreWarts
          case t if hasWartAnnotation(u)(t) =>

          // Ignore usage in synthetic classes
          case ClassDef(_, _, _, _) if synthetic => 

          // Ignore synthetic equals()
          case DefDef(_, EqualsName, _, _, _, _) if synthetic => 

          // Pattern matcher writes var x1 = null.asInstanceOf[...]
          case ValDef(mods, _, _, _) if mods.hasFlag(Flag.MUTABLE) && synthetic =>

          // Ignore allowed casts
          case TypeApply(Select(_, AsInstanceOfName), List(tt))
            if tt.isType && allowedCasts.contains(tt.tpe.typeSymbol.fullName) =>

          // Otherwise it's verboten for non-synthetic exprs
          case Select(e, AsInstanceOfName) if !isSynthetic(u)(e) =>
            u.error(tree.pos, "asInstanceOf is disabled")

          case _ => super.traverse(tree)

        }
      }
    }
  }
}


