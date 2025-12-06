package org.wartremover
package warts

private[warts] trait PartialFunctionApplyCompat { self: PartialFunctionApply.type =>
  final def seqTypeName: String = "scala.collection.SeqLike"
  final def mapTypeName: String = "scala.collection.MapLike"
}
