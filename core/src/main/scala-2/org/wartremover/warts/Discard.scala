package org.wartremover
package warts

object Discard extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    // 他にも検知したい型を列挙しよう！
    val types: Seq[Type] = Seq(
      "scala.concurrent.Future",
    ).map(x => rootMirror.staticClass(x).toTypeConstructor)

    def check(t: Type): Boolean = {
      types.exists(_ =:= t.dealias.typeConstructor)
    }

    new Traverser {
      def msg(typeName: String): String = s"`${typeName}`の値を捨てている可能性があります"

      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          case Block(values, _) =>
            values.withFilter(_.tpe != null).foreach { x =>
              if (check(x.tpe)) {
                error(u)(x.pos, msg(x.tpe.toString))
              }
            }
            super.traverse(tree)
          case t: Template =>
            t.body.withFilter(_.tpe != null).foreach { x =>
              if (check(x.tpe)) {
                error(u)(x.pos, msg(x.tpe.toString))
              }
            }
            super.traverse(tree)
          case f: Function =>
            val params = f.vparams.filter(x => check(x.tpt.tpe))
            val bodyNames = Set.newBuilder[String]
            if (params.nonEmpty) {
              val traverser = new Traverser {
                override def traverseName(name: Name): Unit = {
                  bodyNames += name.toString
                }
              }
              traverser.traverse(f.body)
              val bodyNamesSet = bodyNames.result()
              params.filterNot(p => bodyNamesSet(p.name.toString)).foreach { x =>
                error(u)(x.pos, msg(x.tpt.tpe.toString))
              }
            }
            super.traverse(tree)
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
