package org.wartremover
package warts

object TripleQuestionMark
  extends ExprMatch({ case '{ scala.Predef.??? } =>
    "??? is disabled"
  })
