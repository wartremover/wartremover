package org.wartremover
package warts

object Overloading extends WartTraverser {
  private final case class Sig(param: List[String | Int], result: String)

  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        tree match {
          case t if hasWartAnnotation(t) =>
          case t: ClassDef =>
            val parentMethods: Map[String, Seq[Sig]] = (
              t.parents.flatMap(_.symbol.methodMembers) ++
                TypeRepr.of[AnyRef].classSymbol.toList.flatMap(_.methodMembers)
            ).groupMap(_.name)(sym => Sig(sym.signature.paramSigs, sym.signature.resultSig))

            val methods = t.body.collect {
              case d: DefDef if d.symbol.allOverriddenSymbols.isEmpty && !d.symbol.flags.is(Flags.Synthetic) =>
                d
            }
            val overloads = methods
              .groupBy(_.name)
              .map { (k, v) =>
                k -> v.filterNot(t => hasWartAnnotation(t))
              }
              .filter {
                case (k, Seq(v)) =>
                  parentMethods.get(k) match {
                    case Some(signatures) =>
                      val s = v.symbol.signature
                      val sig = Sig(s.paramSigs, s.resultSig)
                      signatures.exists(_ != sig)
                    case None =>
                      false
                  }
                case _ =>
                  true
              }
              .map(_._2)

            overloads.flatten.foreach { method =>
              error(method.pos, "Overloading is disabled")
            }
            super.traverseTree(tree)(owner)
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
