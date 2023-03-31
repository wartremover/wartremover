package wartremover

import wartremover.InspectWart.Type

private[wartremover] case class InspectArgs(
  sources: Seq[String],
  warts: Seq[Wart]
)

private[wartremover] object InspectArgs {
  def from(values: Seq[InspectArg.Wart]): Map[Type, InspectArgs] = {
    val warnSources = List.newBuilder[String]
    val warnWarts = List.newBuilder[Wart]
    val errorSources = List.newBuilder[String]
    val errorWarts = List.newBuilder[Wart]
    values.foreach {
      case InspectArg.Wart(x: InspectWart.FromSource, Type.Warn) =>
        warnSources ++= x.getSourceContents()
      case InspectArg.Wart(x: InspectWart.FromSource, Type.Err) =>
        errorSources ++= x.getSourceContents()
      case InspectArg.Wart(x: InspectWart.WartName, Type.Warn) =>
        warnWarts += Wart.custom(x.value)
      case InspectArg.Wart(x: InspectWart.WartName, Type.Err) =>
        errorWarts += Wart.custom(x.value)
    }
    Map(
      Type.Warn -> InspectArgs(
        sources = warnSources.result(),
        warts = warnWarts.result()
      ),
      Type.Err -> InspectArgs(
        sources = errorSources.result(),
        warts = errorWarts.result()
      ),
    )
  }
}
