package hochgi.repro.endpoints

import hochgi.repro.datatypes.YConf.{Y1Conf, Y2Conf, Y3Conf}
import hochgi.repro.datatypes.YEnum.{Y1, Y2, Y3}
import hochgi.repro.datatypes._
import io.circe.generic.auto._
import io.circe.{Decoder, DecodingFailure, HCursor, parser, Codec => _}
import sttp.model.StatusCode
import sttp.tapir.Codec.{JsonCodec, PlainCodec}
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{endpoint => ep, _}

object Base {

  val api: PublicEndpoint[Unit, ManagedError, Unit, Any] =
    ep.in("api")
      .errorOut(oneOf[ManagedError](
        oneOfVariant[NotFound](
          statusCode(StatusCode.NotFound)
            .and(jsonBody[NotFound].description("Not Found"))),
        oneOfVariant[UserError](
          statusCode(StatusCode.BadRequest)
            .and(jsonBody[UserError].description("User Error"))),
        oneOfVariant[NoAuth](
          statusCode(StatusCode.Unauthorized)
            .and(jsonBody[NoAuth].description("Unauthorized"))),
        oneOfVariant[Forbidden](
          statusCode(StatusCode.Forbidden)
            .and(jsonBody[Forbidden].description("Forbidden"))),
        oneOfVariant[NotImplemented](
          statusCode(StatusCode.NotImplemented)
            .and(jsonBody[NotImplemented].description("Not Implemented"))),
        oneOfDefaultVariant(jsonBody[Unknown].description("unknown"))
      ))

  implicit val yEnumSchema: Schema[YEnum] = Schema
    .derivedEnumeration[YEnum](encode = Some(_.toString))
    .name(Schema.SName("YEnum"))
    .description("Definition for \"YEnum\" " + YEnum.all.map(_.toString).mkString("(", ", ", ")"))

  implicit val yEnumCodec: PlainCodec[YEnum] =
    Codec.derivedEnumeration[String, YEnum](
      decode = YEnum.unapply,
      encode = _.toString,
      default = None)

  implicit val xEnumSchema: Schema[XEnum] = Schema
    .derivedEnumeration[XEnum](encode = Some(_.toString))
    .name(Schema.SName("XEnum"))
    .description("Definition for \"XEnum\" " + XEnum.all.map(_.toString).mkString("(", ", ", ")"))

  implicit val xEnumCodec: PlainCodec[XEnum] =
    Codec.derivedEnumeration[String, XEnum](
      decode = XEnum.unapply,
      encode = _.toString,
      default = None)

  implicit val yConfSchema: Schema[YConf] = {
    val y1ConfSchema: Schema[YConf.Y1Conf] =
      Schema
        .derived[YConf.Y1Conf]
        .name(Schema.SName("Y1Conf"))

    val y2ConfSchema: Schema[YConf.Y2Conf] =
      Schema
        .derived[YConf.Y2Conf]
        .name(Schema.SName("Y2Conf"))

    val y3ConfSchema: Schema[YConf.Y3Conf] =
      Schema
        .derived[YConf.Y3Conf]
        .name(Schema.SName("Y3Conf"))

    Schema.oneOfUsingField[YConf, String](_.kind, identity[String])(
      YEnum.Y1.toString -> y1ConfSchema,
      YEnum.Y2.toString -> y2ConfSchema,
      YEnum.Y3.toString -> y3ConfSchema)
  }

  private[this] def decodeYConf(c: HCursor, kind: String): Decoder.Result[YConf] = YEnum.unapply(kind) match {
    case None => Left(DecodingFailure(s"$kind cannot be inferred as YEnum", Nil))
    case Some(Y1) => c.downField("size").as[Int].map(Y1Conf.apply)
    case Some(Y2) => c.downField("name").as[String].map(Y2Conf.apply)
    case Some(Y3) => for {
      depth <- c.downField("depth").as[Int]
      model <- c.downField("model").as[String]
    } yield Y3Conf(depth, model)
  }

  implicit val yConfCodec: JsonCodec[YConf] = Codec.json[YConf] { s =>
    val either = for {
      j <- parser.parse(s)
      c  = j.hcursor
      k <- c.downField("kind").as[String]
      y <- decodeYConf(c, k)
    } yield y

    either.fold(DecodeResult.Error(s, _), DecodeResult.Value.apply)
  }{
    case Y1Conf(size) => "{\"kind\":\"Y1\",\"size\":" + size + "}"
    case Y2Conf(name) => "{\"kind\":\"Y2\",\"name\":\"" + name + "\"}"
    case Y3Conf(d, m) => "{\"kind\":\"Y3\",\"depth\":" + d + ",\"model\":\"" + m + "\"}"
  }

  implicit val listOfYConfSchema: Schema[List[YConf]] = yConfSchema.asIterable[List]

  implicit val xConfSchema: Schema[XConf] = Schema
    .derived[XConf]
    .modify(_.kind)(_.copy(schemaType = xEnumSchema.schemaType))
    .modify(_.ys.each)(_.copy(schemaType = yConfSchema.schemaType))

  val getX: Endpoint[Unit, Int, ManagedError, XConf, Any] =
    api.in(path[Int].validate(Validator.inRange(1, 3))).out(jsonBody[XConf])
}
