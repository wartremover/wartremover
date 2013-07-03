package org.brianmckenna.wartremover

import language.experimental.{ macros => scalaMacros }
import reflect.macros.Context

object macros {
  def safe[A](expr: A) = macro safeImpl[A]

  def safeImpl[A](c: Context)(expr: c.Expr[A]): c.Expr[A] = {
    import c.universe._

    def isIgnoredStatement(tree: Tree) = tree match {
      // Scala creates synthetic blocks with <init> calls on classes.
      // The calls return Object so we need to ignore them.
      case Apply(Select(_, nme.CONSTRUCTOR), _) => true
      case _ => false
    }

    object NoNonUnitStatements extends Traverser {
      def checkUnitLike(statements: List[Tree]) {
        statements.foreach { stat =>
          val unitLike = stat.isEmpty || stat.tpe == null || stat.tpe =:= typeOf[Unit] || stat.isDef || isIgnoredStatement(stat)
          if (!unitLike)
            c.error(stat.pos, "Statements must return Unit")
        }
      }

      override def traverse(tree: Tree) {
        tree match {
          case Block(statements, _) =>
            checkUnitLike(statements)
          case ClassDef(_, _, _, Template((_, _, statements))) =>
            checkUnitLike(statements)
          case ModuleDef(_, _, Template((_, _, statements))) =>
            checkUnitLike(statements)
          case _ =>
        }
        super.traverse(tree)
      }
    }
    NoNonUnitStatements.traverse(expr.tree)

    object NoNull extends Traverser {
      val UnapplyName: TermName = "unapply"
      override def traverse(tree: Tree) {
        tree match {
          // Ignore synthetic case class's companion object unapply
          case ModuleDef(mods, _, Template(parents, self, stats)) =>
            mods.annotations foreach { annotation =>
              traverse(annotation)
            }
            parents foreach { parent =>
              traverse(parent)
            }
            traverse(self)
            stats filter {
              case DefDef(_, UnapplyName, _, _, _, _) =>
                false
              case _ =>
                true
            } foreach { stat =>
              traverse(stat)
            }
          case Literal(Constant(null)) =>
            c.error(tree.pos, "null is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
    NoNull.traverse(expr.tree)

    object NoVar extends Traverser {
      val HashCodeName: TermName = "hashCode"
      override def traverse(tree: Tree) {
        tree match {
          // Ignore case class's synthetic hashCode
          case ClassDef(mods, _, tparams, Template(parents, self, stats)) if mods.hasFlag(Flag.CASE) =>
            mods.annotations foreach { annotation =>
              traverse(annotation)
            }
            tparams foreach { tparam =>
              traverse(tparam)
            }
            parents foreach { parent =>
              traverse(parent)
            }
            traverse(self)
            stats filter {
              case DefDef(_, HashCodeName, _, _, _, _) =>
                false
              case _ =>
                true
            } foreach { stat =>
              traverse(stat)
            }
          case ValDef(mods, _, _, _) if mods.hasFlag(Flag.MUTABLE) =>
            c.error(tree.pos, "var is disabled")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
    NoVar.traverse(expr.tree)

    object NoNothing extends Traverser {
      val nothingSymbol = typeOf[Nothing].typeSymbol
      override def traverse(tree: Tree) {
        def error() = c.error(tree.pos, "Inferred type containing Nothing from assignment")
        tree match {
          case ValDef(_, _, tpt, _) if tpt.tpe.contains(nothingSymbol) =>
            error()
          case DefDef(_, _, _, _, tpt, _) if tpt.tpe.contains(nothingSymbol) =>
            error()
          case _ =>
        }
        super.traverse(tree)
      }
    }
    NoNothing.traverse(expr.tree)

    expr
  }
}
