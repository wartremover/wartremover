package org.wartremover
package warts

object IterableOps extends WartTraverser {
  private val methodNames: Seq[String] = Seq(
    "head",
    "tail",
    "init",
    "last",
    "reduce",
    "max",
    "min",
  )

  private object Method {
    def unapply(s: String): Option[String] = PartialFunction.condOpt(s) {
      case "head" =>
        "headOption"
      case "tail" =>
        "drop(1)"
      case "init" =>
        "dropRight(1)"
      case "last" =>
        "lastOption"
      case "reduce" =>
        "reduceOption or fold"
      case "reduceLeft" =>
        "reduceLeftOption or foldLeft"
      case "reduceRight" =>
        "reduceRightOption or foldRight"
      case "maxBy" =>
        "foldLeft or foldRight"
      case "max" =>
        "foldLeft or foldRight"
      case "minBy" =>
        "foldLeft or foldRight"
      case "min" =>
        "foldLeft or foldRight"
    }
  }

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      private val iterableSymbol = Symbol.requiredClass("scala.collection.Iterable")
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case _ if methodNames.forall(sourceCodeNotContains(tree, _)) =>
          case t if hasWartAnnotation(t) =>
          case s @ Select(t, method @ Method(alternative)) if t.tpe.derivesFrom(iterableSymbol) =>
            error(selectNamePosition(s), s"${method} is disabled - use ${alternative} instead")
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
