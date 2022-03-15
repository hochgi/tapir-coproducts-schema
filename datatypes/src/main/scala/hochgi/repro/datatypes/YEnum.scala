package hochgi.repro.datatypes

sealed trait YEnum
object YEnum {
  case object Y1 extends YEnum
  case object Y2 extends YEnum
  case object Y3 extends YEnum

  def all: List[YEnum] = List(Y1, Y2, Y3)

  def unapply(y: String): Option[YEnum] = y.toLowerCase match {
    case "y1" => Some(Y1)
    case "y2" => Some(Y2)
    case "y3" => Some(Y3)
    case _ => None
  }
}
