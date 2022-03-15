package hochgi.repro.datatypes

sealed trait XEnum
object XEnum {
  case object X1 extends XEnum
  case object X2 extends XEnum
  case object X3 extends XEnum

  def all: List[XEnum] = List(X1, X2, X3)

  def unapply(x: String): Option[XEnum] = x.toLowerCase match {
    case "x1" => Some(X1)
    case "x2" => Some(X2)
    case "x3" => Some(X3)
    case _ => None
  }
}
