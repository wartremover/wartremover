package org.wartremover
package warts

object NoNeedForMonad extends WartTraverser {
  val message = """No need for Monad here (Applicative should suffice).
                 | > "If the extra power provided by Monad isn’t needed, it’s usually a good idea to use Applicative instead."
                 | Typeclassopedia (http://www.haskell.org/haskellwiki/Typeclassopedia)
                 | Apart from a cleaner code, using Applicatives instead of Monads can in general case result in a more parallel code.
                 | For more context, please refer to the aforementioned Typeclassopedia, http://comonad.com/reader/2012/abstracting-with-applicatives/, or http://www.serpentine.com/blog/2008/02/06/the-basics-of-applicative-functors-put-to-practical-work/""".stripMargin
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def processForComprehension(tree: Tree, enums: List[Tree], body: Tree): Unit = {
      val bindings = enums.flatMap {
        case fq"$lhs <- $rhs" =>
          lhs match {
            case Bind(name, _) => List((Ident(name): Tree, rhs))
            case Apply(_, bindings: List[Tree]) => bindings.map {
              case Bind(name, _) => (Ident(name): Tree, rhs)
            }
          }
        case _ =>
          Nil
      }.toMap

      val names = bindings.keys
      val rhss  = bindings.values

      val result = rhss.exists { rhs =>
        names.exists(name => rhs.exists(_ equalsStructure name))
      }

      if(bindings.size > 1 && !result) {
        warning(u)(tree.pos, message)
      }
    }

    def processFlatMapChain(tree: Tree): Unit = {
      def extractFuns(fname: String, tree: Tree): List[Tree] =
        tree.collect {
          case Apply(          Select(_, termName),     fn)
              if (termName.toString == fname) => fn
          case Apply(TypeApply(Select(_, termName), _), fn)
              if (termName.toString == fname) => fn
        }.flatten

      def asFuncTransform(args: List[Tree], body: Tree) =
        (args.map { case arg @ ValDef(_, name, _, _) =>
           Ident(name): Tree
         }, body)

      def processBodyArgs(tr: Tree) = tr match {
        case q"(..$args) => $body" => Some(asFuncTransform(args, body))
        case Block(args, body)     => Some(asFuncTransform(args, body))
        case x                     => None
      }

      val subTreeBody = extractFuns("map", tree).flatMap(t => processBodyArgs(t))
      val subTreeFun  = extractFuns("flatMap", tree).flatMap(t => processBodyArgs(t))

      if(!subTreeBody.isEmpty && !subTreeFun.isEmpty) {
        val yields  = subTreeBody.map(_._2)
        val results = subTreeFun.flatMap { case (args, body) =>
          args.map { arg =>
            // Argument should occur in the body of the function the number of times
            // it occurs in the yield statement
            // (i.e. only occurrences in the yield statement are allowed).
            val countInYield = yields.flatMap(_.filter(_ equalsStructure arg)).size
            body.filter(_ equalsStructure arg).size == countInYield
          }
        }

        if (results.forall(identity))
          warning(u)(tree.pos, message)
      }
    }

    new u.Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>
          // Note: first two cases currently don't not work in 2.10: https://github.com/scalamacros/paradise/issues/38
          // Will propagate to matching desugared chain of maps/flatMaps.
          case q"for (..$enums) yield $body"                                                        => processForComprehension(tree, enums, body)
          case q"for (..$enums) $body"                                                              => processForComprehension(tree, enums, body)
          case tr @ Apply(          Select(_, termName),     _) if (termName.toString == "flatMap") => processFlatMapChain(tr)
          case tr @ Apply(TypeApply(Select(_, termName), _), _) if (termName.toString == "flatMap") => processFlatMapChain(tr)
          case _  => super.traverse(tree)
        }
      }
    }
  }
}
