package org.wartremover
package warts

object ScalaApp
    extends ForbidType[scala.App](
      "Don't use scala.App. https://docs.scala-lang.org/scala3/book/methods-main-methods.html"
    )
