package org.wartremover
package warts

import scala.quoted.Expr
import scala.quoted.quotes

object SizeToLength
    extends ExprMatch({
      val message = "Maybe you should use `length` instead of `size`"
      import quotes.reflect.*
      val f: PartialFunction[Expr[Any], String] = {
        case '{ Predef.genericArrayOps[t]($x1).size } =>
          message
        case '{ Predef.booleanArrayOps($x1).size } =>
          message
        case '{ Predef.byteArrayOps($x1).size } =>
          message
        case '{ Predef.charArrayOps($x1).size } =>
          message
        case '{ Predef.doubleArrayOps($x1).size } =>
          message
        case '{ Predef.floatArrayOps($x1).size } =>
          message
        case '{ Predef.intArrayOps($x1).size } =>
          message
        case '{ Predef.longArrayOps($x1).size } =>
          message
        case '{
              type t <: AnyRef
              Predef.refArrayOps[`t`]($x1).size
            } =>
          message
        case '{ Predef.shortArrayOps($x1).size } =>
          message
        case '{ Predef.unitArrayOps($x1).size } =>
          message
        case '{ Predef.augmentString($x1).size } =>
          message
      }
      f
    })
