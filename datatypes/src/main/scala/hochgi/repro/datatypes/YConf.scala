package hochgi.repro.datatypes

sealed trait YConf {
  def kind: String
}
object YConf {
  final case class Y1Conf(size: Int) extends YConf {
    override def kind: String = YEnum.Y1.toString
  }

  final case class Y2Conf(name: String) extends YConf {
    override def kind: String = YEnum.Y2.toString
  }

  final case class Y3Conf(depth: Int, model: String) extends YConf {
    override def kind: String = YEnum.Y3.toString
  }
}
