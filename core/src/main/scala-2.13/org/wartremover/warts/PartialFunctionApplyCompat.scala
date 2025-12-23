package org.wartremover
package warts

private[warts] trait PartialFunctionApplyCompat { self: PartialFunctionApply.type =>
  final def seqTypeName: String = "scala.collection.SeqOps"
  final def mapTypeName: String = "scala.collection.MapOps"
}
