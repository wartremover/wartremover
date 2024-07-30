package org.wartremover
package warts

object Discard {
  object Either extends Discard(Set("scala.util.Either"), true)
  object Future extends Discard(Set("scala.concurrent.Future"), true)
  object Try extends Discard(Set("scala.util.Try"), true)
}

abstract class Discard(types: Set[String], subtype: Boolean) extends WartTraverser {
  override def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    val checkTypes: Seq[Type] = types.toSeq.map(x => rootMirror.staticClass(x).toTypeConstructor)

    def check(t: Type): Boolean = {
      if (subtype) {
        checkTypes.exists(t.dealias.typeConstructor <:< _)
      } else {
        checkTypes.exists(t.dealias.typeConstructor =:= _)
      }
    }

    def msg(t: Type): String =
      s"discard `${t.dealias}`"

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) || isSyntheticPartialFunction(u)(t) =>
          case Block(values, _) =>
            values.withFilter(_.tpe != null).foreach { x =>
              if (check(x.tpe)) {
                error(u)(x.pos, msg(x.tpe))
              }
            }
            super.traverse(tree)
          case t: Template =>
            t.body.withFilter(_.tpe != null).foreach { x =>
              if (check(x.tpe)) {
                error(u)(x.pos, msg(x.tpe))
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
                error(u)(x.pos, msg(x.tpt.tpe))
              }
            }
            super.traverse(tree)
          case f: CaseDef if check(f.pat.tpe) =>
            PartialFunction
              .condOpt(f.pat) {
                case Bind(TermName(name), Ident(TermName("_"))) =>
                  name
                case x: Ident =>
                  x.name.toString
              }
              .foreach { name =>
                val names = Set.newBuilder[String]
                val traverser = new Traverser {
                  override def traverseName(name: Name): Unit = {
                    names += name.toString
                  }
                }
                traverser.traverse(f.guard)
                traverser.traverse(f.body)
                val namesSet = names.result()
                if (namesSet(name)) {
                  // ok
                } else {
                  error(u)(f.pat.pos, msg(f.pat.tpe))
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
