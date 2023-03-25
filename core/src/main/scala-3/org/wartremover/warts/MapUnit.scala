package org.wartremover
package warts

import scala.quoted.Expr
import scala.quoted.Type
import scala.quoted.Quotes
import scala.quoted.quotes

object MapUnit
    extends ExprMatch({
      import quotes.reflect.*

      val f: PartialFunction[Expr[Any], String] = {
        case '{
              type t1
              type t2
              ($x: collection.Iterable[`t1`]).map($f: Function1[`t1`, `t2`])
            } if TypeRepr.of[t2] =:= TypeRepr.of[Unit] =>
          "Maybe you should use `foreach` instead of `map`"
      }

      f
    })
