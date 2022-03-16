package hochgi.repro.datatypes

sealed trait YConf {
  def kind: YEnum
}
object YConf {
  final case class Y1Conf(size: Int) extends YConf {
    override def kind: YEnum = YEnum.Y1
  }

  final case class Y2Conf(name: String) extends YConf {
    override def kind: YEnum = YEnum.Y2
  }

  final case class Y3Conf(depth: Int, model: String) extends YConf {
    override def kind: YEnum = YEnum.Y3
  }
}
