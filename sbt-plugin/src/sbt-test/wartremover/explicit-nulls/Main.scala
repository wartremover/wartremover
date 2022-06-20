package example

@SuppressWarnings(Array("org.wartremover.warts.Null"))
def useNull(n: Int): String | Null =
  if n < 0 then null else n.toString

@SuppressWarnings(Array("org.wartremover.warts.FinalVal"))
final val n = 10
