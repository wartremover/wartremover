package org.wartremover.warts

import org.wartremover.ExprMatch

object AutoUnboxing
    extends ExprMatch({
      case '{ Byte2byte($x) } =>
        "java.lang.Byte to scala.Byte auto unboxing"
      case '{ Short2short($x) } =>
        "java.lang.Short to scala.Short auto unboxing"
      case '{ Character2char($x) } =>
        "java.lang.Character to scala.Char auto unboxing"
      case '{ Integer2int($x) } =>
        "java.lang.Integer to scala.Int auto unboxing"
      case '{ Long2long($x) } =>
        "java.lang.Long to scala.Long auto unboxing"
      case '{ Float2float($x) } =>
        "java.lang.Float to scala.Float auto unboxing"
      case '{ Double2double($x) } =>
        "java.lang.Double to scala.Double auto unboxing"
      case '{ Boolean2boolean($x) } =>
        "java.lang.Boolean to scala.Boolean auto unboxing"
    })
