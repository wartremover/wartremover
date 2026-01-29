package org.wartremover
package warts

object IterableOps extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val symbol = rootMirror.staticClass("scala.collection.Iterable")
    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          case Select(left, TermName(Method(message))) if left.tpe.baseType(symbol) != NoType =>
            error(u)(tree.pos, message)
          // TODO: This ignores a lot
          case LabelDef(_, _, rhs) if isSynthetic(u)(tree) =>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }

  private object Method {
    def unapply(s: String): Option[String] = PartialFunction.condOpt(s) {
      case "head" =>
        "head is disabled - use headOption instead"
      case "tail" =>
        "tail is disabled - use drop(1) instead"
      case "init" =>
        "init is disabled - use dropRight(1) instead"
      case "last" =>
        "last is disabled - use lastOption instead"
      case "reduce" =>
        "reduce is disabled - use reduceOption or fold instead"
      case "reduceLeft" =>
        "reduceLeft is disabled - use reduceLeftOption or foldLeft instead"
      case "reduceRight" =>
        "reduceRight is disabled - use reduceRightOption or foldRight instead"
      case "maxBy" =>
        "maxBy is disabled - use foldLeft or foldRight instead"
      case "max" =>
        "max is disabled - use foldLeft or foldRight instead"
      case "minBy" =>
        "minBy is disabled - use foldLeft or foldRight instead"
      case "min" =>
        "min is disabled - use foldLeft or foldRight instead"
    }
  }
}
