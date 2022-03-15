package hochgi.repro.logic

import hochgi.repro.datatypes.YConf.{Y1Conf, Y2Conf, Y3Conf}
import hochgi.repro.datatypes.{ManagedError, UserError, XConf}

import scala.util.{Random => R}

object GetXConf {

  def get(input: Int): Either[ManagedError, XConf] = input match {
    case 1 => Right(XConf.x1(Y1Conf(R.nextInt()), Y2Conf(R.alphanumeric.take(7).mkString)))
    case 2 => Right(XConf.x2(Y1Conf(R.nextInt()), Y1Conf(R.nextInt()), Y3Conf(R.nextInt(), R.alphanumeric.take(7).mkString)))
    case 3 => Right(XConf.x3(Y1Conf(R.nextInt()), Y2Conf(R.alphanumeric.take(7).mkString), Y3Conf(R.nextInt(), R.alphanumeric.take(7).mkString)))
    case u => Left(UserError(s"[$u] is not valid!"))
  }
}
