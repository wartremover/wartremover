package org.wartremover
package warts

object NoNeedImport extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    import u.universe._

    def isRename(origName: Name, rename: Name) = {
      val maybeRename = Option(rename) // 'rename' can be null

      maybeRename.exists(_ != origName)
    }

    new Traverser {
      override def traverse(tree: Tree): Unit = {
        tree match {
          case t if hasWartAnnotation(u)(t) =>
          // Ignore trees marked by SuppressWarnings

          case Import(_, iss) =>
            iss.foldLeft(ImportTypeContainer.init) {
              case (acc, ImportSelector(termNames.WILDCARD, _, _, _)) =>
                // If `import aaa.bbb.{ _ => ccc }` or `import aaa.bbb.{ _ => _ }`
                // would be written, it should be the same meaning of
                // `import aaa.bbb._` so we don't need to care the 3rd parameter of the `ImportSelector`
                acc.copy(existsWildCard = true)
              case (acc, ImportSelector(_, _, termNames.WILDCARD, _)) =>
                acc.copy(existsNameToWildCard = true)
              case (acc, ImportSelector(origName, _, rename, _)) if !isRename(origName, rename) =>
                // We are avoiding rename imports like `import aaa.bbb.{ ccc => ddd } since these
                // are valid in the context of wildcard imports
                acc.copy(existsRegular = true)
              case (acc, _) =>
                acc
            } match {
              case ImportTypeContainer(true, true, _) =>
              // In this case, there are `import aaa.bbb.{ ccc => _, ddd, _ }`.
              // It means that all should be imported except `ccc`.
              case ImportTypeContainer(true, false, _) =>
                // In case `import aaa.bbb.{ ccc => _, ddd }`,
                // a programmer could remove `ccc => _`.
                error(u)(tree.pos, "Import into the wildcard(`something => _`) is meaningless. Remove it.")
              case ImportTypeContainer(false, true, true) =>
                // In case there is a wildcard and at least one regular (non-rename) import
                // (the case where there is a renamed import and a wildcard is okay)
                error(u)(tree.pos, "The wildcard import exists. Remove other explicitly names of the `import`.")
              case _ =>
            }

          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}
