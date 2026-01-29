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
          case Apply(method, _ :: Nil) =>
            method.symbol.fullName match {
              case "scala.Predef.Byte2byte" =>
                error(u)(tree.pos, "java.lang.Byte to scala.Byte auto unboxing")
              case "scala.Predef.Short2short" =>
                error(u)(tree.pos, "java.lang.Short to scala.Short auto unboxing")
              case "scala.Predef.Character2char" =>
                error(u)(tree.pos, "java.lang.Character to scala.Char auto unboxing")
              case "scala.Predef.Integer2int" =>
                error(u)(tree.pos, "java.lang.Integer to scala.Int auto unboxing")
              case "scala.Predef.Long2long" =>
                error(u)(tree.pos, "java.lang.Long to scala.Long auto unboxing")
              case "scala.Predef.Float2float" =>
                error(u)(tree.pos, "java.lang.Float to scala.Float auto unboxing")
              case "scala.Predef.Double2double" =>
                error(u)(tree.pos, "java.lang.Double to scala.Double auto unboxing")
              case "scala.Predef.Boolean2boolean" =>
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
