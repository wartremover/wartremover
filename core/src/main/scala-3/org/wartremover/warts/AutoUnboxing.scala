package org.wartremover
package warts

object AutoUnboxing extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val methods: Seq[(Symbol, String)] = Seq(
        Symbol.requiredMethod("scala.Predef.Byte2byte") -> "java.lang.Byte to scala.Byte auto unboxing",
        Symbol.requiredMethod("scala.Predef.Short2short") -> "java.lang.Short to scala.Short auto unboxing",
        Symbol.requiredMethod("scala.Predef.Character2char") -> "java.lang.Character to scala.Char auto unboxing",
        Symbol.requiredMethod("scala.Predef.Integer2int") -> "java.lang.Integer to scala.Int auto unboxing",
        Symbol.requiredMethod("scala.Predef.Long2long") -> "java.lang.Long to scala.Long auto unboxing",
        Symbol.requiredMethod("scala.Predef.Float2float") -> "java.lang.Float to scala.Float auto unboxing",
        Symbol.requiredMethod("scala.Predef.Double2double") -> "java.lang.Double to scala.Double auto unboxing",
        Symbol.requiredMethod("scala.Predef.Boolean2boolean") -> "java.lang.Boolean to scala.Boolean auto unboxing",
      )
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case Apply(method, _ :: Nil) =>
            methods.find(_._1 == method.symbol).foreach { (_, message) =>
              error(tree.pos, message)
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
