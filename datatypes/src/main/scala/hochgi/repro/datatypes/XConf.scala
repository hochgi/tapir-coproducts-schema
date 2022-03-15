package hochgi.repro.datatypes

import hochgi.repro.datatypes.YConf.{Y1Conf, Y2Conf, Y3Conf}

final case class XConf(kind: XEnum, ys: List[YConf])
object XConf {
  def x1(y1: Y1Conf, y2: Y2Conf): XConf = XConf(XEnum.X1, List(y1, y2))
  def x2(y11: Y1Conf, y12: Y1Conf, y3: Y3Conf): XConf = XConf(XEnum.X2, List(y11, y12, y3))
  def x3(y1: Y1Conf, y2: Y2Conf, y3: Y3Conf): XConf = XConf(XEnum.X3, List(y1, y2, y3))
}
