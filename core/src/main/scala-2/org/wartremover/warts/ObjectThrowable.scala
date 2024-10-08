package org.wartremover
package warts

object ObjectThrowable extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._
    new Traverser {
      private val scalaNoStackTrace = rootMirror.staticClass("scala.util.control.NoStackTrace").toType
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case t: ModuleDef if {
                val tpe = t.symbol.typeSignature.typeSymbol.asType.toType
                (tpe <:< typeOf[java.lang.Throwable]) && !(tpe <:< scalaNoStackTrace)
              } =>
            error(u)(t.pos, "use class if extends Throwable")
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
