package org.wartremover.warts

/**
 * @param existsNameToWildCard  Exists `import aaa.bbb.{ ccc => _ }` or not
 * @param existsWildCard        Exists `import aaa.bbb.{ _ }` or not
 * @param existsRegular         Exists `import aaa.bbb.{ c }` or not
 */
private[warts] final case class ImportTypeContainer(
  existsNameToWildCard: Boolean,
  existsWildCard: Boolean,
  existsRegular: Boolean
)

private[warts] object ImportTypeContainer {
  val init: ImportTypeContainer =
    ImportTypeContainer(existsNameToWildCard = false, existsWildCard = false, existsRegular = false)
}
