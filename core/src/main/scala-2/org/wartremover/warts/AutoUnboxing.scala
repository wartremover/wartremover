package org.wartremover.warts

import org.wartremover.WartTraverser
import org.wartremover.WartUniverse

object AutoUnboxing extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case _ if hasWartAnnotation(u)(tree) =>
          case q"scala.Predef.Byte2byte($x)" =>
            error(u)(tree.pos, "java.lang.Byte to scala.Byte auto unboxing")
          case q"scala.Predef.Short2short($x)" =>
            error(u)(tree.pos, "java.lang.Short to scala.Short auto unboxing")
          case q"scala.Predef.Character2char($x)" =>
            error(u)(tree.pos, "java.lang.Character to scala.Char auto unboxing")
          case q"scala.Predef.Integer2int($x)" =>
            error(u)(tree.pos, "java.lang.Integer to scala.Int auto unboxing")
          case q"scala.Predef.Long2long($x)" =>
            error(u)(tree.pos, "java.lang.Long to scala.Long auto unboxing")
          case q"scala.Predef.Float2float($x)" =>
            error(u)(tree.pos, "java.lang.Float to scala.Float auto unboxing")
          case q"scala.Predef.Double2double($x)" =>
            error(u)(tree.pos, "java.lang.Double to scala.Double auto unboxing")
          case q"scala.Predef.Boolean2boolean($x)" =>
            error(u)(tree.pos, "java.lang.Boolean to scala.Boolean auto unboxing")
          case Apply(Select(Select(This(TypeName("scala")), TermName("Predef")), TermName(method)), _) =>
            // Scala 2.11
            method match {
              case "Byte2byte" =>
                error(u)(tree.pos, "java.lang.Byte to scala.Byte auto unboxing")
              case "Short2short" =>
                error(u)(tree.pos, "java.lang.Short to scala.Short auto unboxing")
              case "Character2char" =>
                error(u)(tree.pos, "java.lang.Character to scala.Char auto unboxing")
              case "Integer2int" =>
                error(u)(tree.pos, "java.lang.Integer to scala.Int auto unboxing")
              case "Long2long" =>
                error(u)(tree.pos, "java.lang.Long to scala.Long auto unboxing")
              case "Float2float" =>
                error(u)(tree.pos, "java.lang.Float to scala.Float auto unboxing")
              case "Double2double" =>
                error(u)(tree.pos, "java.lang.Double to scala.Double auto unboxing")
              case "Boolean2boolean" =>
                error(u)(tree.pos, "java.lang.Boolean to scala.Boolean auto unboxing")
              case _ =>
                super.traverse(tree)
            }
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
