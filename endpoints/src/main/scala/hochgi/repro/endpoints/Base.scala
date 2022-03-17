package hochgi.repro.endpoints

import hochgi.repro.datatypes.YConf.{Y1Conf, Y2Conf, Y3Conf}
import hochgi.repro.datatypes.YEnum.{Y1, Y2, Y3}
import hochgi.repro.datatypes._
import io.circe.generic.auto._
import io.circe.{HCursor, Json, parser, Codec => _}
import sttp.model.StatusCode
import sttp.tapir.Codec.{JsonCodec, PlainCodec}
import sttp.tapir.Schema.SName
import sttp.tapir.SchemaType.{SCoproduct, SDiscriminator, SRef, SchemaWithValue}
import sttp.tapir.generic.Derived
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
    val y1ConfSName = Schema.SName("Y1Conf")
    val y1ConfSchema: Schema[YConf.Y1Conf] =
      Schema
        .derived[YConf.Y1Conf]
        .name(y1ConfSName)

    val y2ConfSName = Schema.SName("Y2Conf")
    val y2ConfSchema: Schema[YConf.Y2Conf] =
      Schema
        .derived[YConf.Y2Conf]
        .name(y2ConfSName)

    val y3ConfSName = Schema.SName("Y3Conf")
    val y3ConfSchema: Schema[YConf.Y3Conf] =
      Schema
        .derived[YConf.Y3Conf]
        .name(y3ConfSName)

//    // This does not work:
//    Schema.oneOfUsingField[YConf, YEnum](_.kind, _.toString)(
//      YEnum.Y1 -> y1ConfSchema,
//      YEnum.Y2 -> y2ConfSchema,
//      YEnum.Y3 -> y3ConfSchema).modify(_.kind)(_ => yEnumSchema)

    // Following is an adjusted code from output
    // when compiling with: TAPIR_LOG_GENERATED_CODE=true
    // and originally using strings instead of enums.
    // And it does work.
    val mappingAsList = List(Y1 -> y1ConfSchema, Y2 -> y2ConfSchema, Y3 -> y3ConfSchema)
    val discriminator = SDiscriminator(FieldName("kind", "kind"), mappingAsList.view.collect {
      case (k, Schema(_, Some(fname), _, _, _, _, _, _, _)) => k.toString -> SRef(fname)
    }.toMap)
    val sname = SName("hochgi.repro.datatypes.YConf", Nil)
    val subtypes = mappingAsList.map(_._2)
    val schemaType = SCoproduct[YConf](subtypes, Some(discriminator)) {
      case y: Y1Conf => Some(SchemaWithValue(y1ConfSchema.asInstanceOf[Schema[Any]], y.kind))
      case y: Y2Conf => Some(SchemaWithValue(y2ConfSchema.asInstanceOf[Schema[Any]], y.kind))
      case y: Y3Conf => Some(SchemaWithValue(y3ConfSchema.asInstanceOf[Schema[Any]], y.kind))
    }.addDiscriminatorField(
      discriminatorName    = FieldName("kind", "kind"),
      discriminatorSchema  = yEnumSchema,
      discriminatorMapping = Map(
        YEnum.Y1.toString -> SRef(y1ConfSName),
        YEnum.Y2.toString -> SRef(y2ConfSName),
        YEnum.Y3.toString -> SRef(y3ConfSName),
      ))
    Schema(schemaType, Some(sname))
  }

  private def decodeYConf(c: HCursor, kind: String): DecodeResult[YConf] = yEnumCodec.decode(kind).flatMap { yEnum =>
    val either = yEnum match {
      case Y1 => c.downField("size").as[Int].map(Y1Conf.apply)
      case Y2 => c.downField("name").as[String].map(Y2Conf.apply)
      case Y3 => for {
        depth <- c.downField("depth").as[Int]
        model <- c.downField("model").as[String]
      } yield Y3Conf(depth, model)
    }

    either.fold[DecodeResult[YConf]](DecodeResult.Error(c.value.noSpaces, _), DecodeResult.Value.apply)
  }

  implicit val yConfCodec: JsonCodec[YConf] = Codec.json[YConf] { s =>
    val either = for {
      j <- parser.parse(s)
      c  = j.hcursor
      k <- c.downField("kind").as[String]
    } yield decodeYConf(c, k)

    either.fold(DecodeResult.Error(s, _), identity)
  }{
    case Y1Conf(size) => "{\"kind\":\"" + yEnumCodec.encode(Y1) + "\",\"size\":" + size + "}"
    case Y2Conf(name) => "{\"kind\":\"" + yEnumCodec.encode(Y2) + "\",\"name\":\"" + name + "\"}"
    case Y3Conf(d, m) => "{\"kind\":\"" + yEnumCodec.encode(Y3) + "\",\"depth\":" + d + ",\"model\":\"" + m + "\"}"
  }(yConfSchema)

  implicit val listOfYConfSchema: Schema[List[YConf]] = yConfSchema.asIterable[List]

  implicit val listOfYConfCodec: Codec[List[String], List[YConf], CodecFormat.Json] = Codec.list(yConfCodec)

  implicit val xConfSchema: Schema[XConf] = implicitly[Derived[Schema[XConf]]]
    .value
    .modify(_.kind)(_.copy(
      schemaType     = xEnumSchema.schemaType,
      name           = xEnumSchema.name,
      isOptional     = xEnumSchema.isOptional,
      description    = xEnumSchema.description,
      default        = xEnumSchema.default,
      format         = xEnumSchema.format,
      encodedExample = xEnumSchema.encodedExample,
      deprecated     = xEnumSchema.deprecated,
      validator      = xEnumSchema.validator)
    )
    .modify(_.ys.each)(_.copy(
      schemaType     = yConfSchema.schemaType,
      name           = yConfSchema.name,
      isOptional     = yConfSchema.isOptional,
      description    = yConfSchema.description,
      default        = yConfSchema.default,
      format         = yConfSchema.format,
      encodedExample = yConfSchema.encodedExample,
      deprecated     = yConfSchema.deprecated,
      validator      = yConfSchema.validator)
    )

  implicit val xConfCodec: Codec[String, XConf, CodecFormat.Json] = Codec.json[XConf]{ s =>
    val either = for {
      j <- parser.parse(s)
      c  = j.hcursor
      k <- c.downField("kind").as[String]
      a <- c.downField("ys").as[List[Json]].map(_.map(_.noSpaces))
    } yield for {
      kind <- xEnumCodec.decode(k)
      ys   <- listOfYConfCodec.decode(a)
    } yield XConf(kind, ys)

    either.fold(DecodeResult.Error(s, _), identity)
  }{ case XConf(kind, ys) =>
    val sb = new StringBuilder
    sb ++= "{\"kind\":\""
    sb ++= xEnumCodec.encode(kind)
    if (ys.isEmpty) sb ++= "\"}"
    else {
      sb ++= "\",\"ys\":"
      var ch = '['
      listOfYConfCodec.encode(ys).foreach { yConfStr =>
        sb += ch
        ch = ','
        sb ++= yConfStr
      }
      sb ++= "]}"
    }
    sb.result()
  }

  val getX: Endpoint[Unit, Int, ManagedError, XConf, Any] = api
    .get
    .in(path[Int].validate(Validator.inRange(1, 3)))
    .out(anyFromUtf8StringBody[XConf, CodecFormat.Json](xConfCodec))
}
