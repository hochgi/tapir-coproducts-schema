import Dependencies._

def depver(scalaVersion: String): Version = scalaVersion match {
  case "2.11" => Version.S211
  case "2.12" => Version.S212
  case "2.13" => Version.S213
  case binver => throw new NotImplementedError(s"Build not defined for scala $binver")
}

val allScalaCrossVersions     = List(Version.S213, Version.S212, Version.S211).map(_.scalaFullVersion)
val scala212n213CrossVersions = List(Version.S213, Version.S212).map(_.scalaFullVersion)
val scala213CrossVersion      = List(Version.S213.scalaFullVersion)

val sharedSettings = {
  Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.hochgi",
    scalaVersion := Version.S213.scalaFullVersion,
    scalacOptions ++= {
      val safeForCrossParams = Seq(
        "-encoding", "UTF-8",    // source files are in UTF-8
        "-deprecation",          // warn about use of deprecated APIs
        "-unchecked",            // warn about unchecked type parameters
        "-feature",              // warn about misused language features
        "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
        "-Xlint"                 // enable handy linter warnings

        // fatal warnings is temporarily off until a workaround is found for:
        // "Block result was adapted via implicit conversion (method apply) taking a by-name parameter"
        // see:
        //   https://users.scala-lang.org/t/2-13-3-by-name-implicit-linting-error/6334/2
        //   https://github.com/scala/scala/pull/8590
        //   https://github.com/scala/bug/issues/12072
        //
        // "-Xfatal-warnings"       // compilation warning fail as errors
      )

      scalaBinaryVersion.value match {
        case "2.11" => safeForCrossParams :+ "-Ypartial-unification" :+ "-language:existentials"
        case "2.12" => safeForCrossParams :+ "-Ypartial-unification"
        case "2.13" => safeForCrossParams :+ "-language:postfixOps"
        case binver => throw new NotImplementedError(s"Build not defined for scala $binver")
      }
    },
    scalacOptions ++= Seq("-target:jvm-1.8"),
    Test / scalacOptions ++= Seq("-target:jvm-1.8"),
    libraryDependencies ++= {
      val v = depver(scalaBinaryVersion.value)
      Seq(
        scalacheck(v) % Test,
        scalatest % Test
      )
    },
    dependencyOverrides ++= {
      val tapirVersion = depver(scalaBinaryVersion.value).tapir.version
      Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"   % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-apispec-docs"       % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-apispec-model"      % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-enumeratum"         % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe"      % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-openapi-model"      % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"        % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui"         % tapirVersion)
    }
  )
}

lazy val datatypes = (project in file("datatypes"))
  .enablePlugins(ScalastylePlugin)
  .settings(
    sharedSettings,
    name := "tapir-repro-datatypes",
    crossScalaVersions := allScalaCrossVersions
  )

lazy val endpoints = (project in file("endpoints"))
  .enablePlugins(ScalastylePlugin)
  .dependsOn(datatypes)
  .settings(
    sharedSettings,
    name := "tapir-repro-endpoints",
    crossScalaVersions := scala213CrossVersion,
    libraryDependencies ++= {
      val v = depver(scalaBinaryVersion.value)
      Seq(
        tapirCore(v),
        tapirJsonCirce(v),
        circeCore(v),
        circeGeneric(v),
        sttp3Akka(v),
        tapirAkkaHttpServer(v),
        scalaLogging,
        commonsText
      )
    }
  )

lazy val logic = (project in file("logic"))
  .enablePlugins(ScalastylePlugin)
  .dependsOn(datatypes)
  .settings(
    sharedSettings,
    name := "tapir-repro-logic",
    crossScalaVersions := scala213CrossVersion,
    libraryDependencies ++= {
      val v = depver(scalaBinaryVersion.value)
      Seq(
//        akkaActor(v),
//        akkaStreamAlpakkaJsonStreaming,
//        eventLoggerFirehoseLogger,
//        eventLoggerEncodeCirce,
//        scalaLogging,
//        logstashLogback,
//        tapirSttpClient(v),
//        sttp3Akka(v),
//        jctools,
//        postgresql,
//        akkaPersistenceTestkit(v) % Test,
//        akkaStreamTestkit(v) % Test,
//        akkaActorTestkitTyped(v) % Test,
//        akkaPersistenceTestkit(v) % Test,
//        scalacheck(v) % Test,
//        logbackClassic % Test,
//        h2Database % Test
      )
    }
  )

lazy val server = (project in file("server"))
  .enablePlugins(ScalastylePlugin, JavaAppPackaging, BuildInfoPlugin, GitVersioning)
  .dependsOn(endpoints, logic)
  .settings(
    sharedSettings,
    name := "tapir-repro-server",
    crossScalaVersions := scala213CrossVersion,
    libraryDependencies ++= {
      val v = depver(scalaBinaryVersion.value)
      Seq(
        akkaHttp(v),
        akkaActor(v),
        akkaStream(v),
        akkaSlf4j(v),
        akkaStreamContrib(v),
        tapirCore(v),
        tapirAkkaHttpServer(v),
        tapirSwaggerUi(v),
        tapirOpenapiDocs(v),
        tapirOpenapiCirceYaml(v),
        tapirJsonCirce(v),
        sttpSharedAkka,
        logbackClassic,
        scalaLogging,
        akkaHttpTestkit(v) % Test,
        scalacheck(v) % Test,
        scalatest % Test
      )
    },
    Universal / mappings ++= {
      val resources = (Compile / resourceDirectory).value
      val cnf = resources / "application.conf"
      val log = resources / "logback.xml"
      Seq(
        cnf -> "conf/application.conf",
        log -> "conf/logback.xml")
    },
    bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
    bashScriptExtraDefines += """addJava "-Dapplication.home=${app_home}/..""""
  )

lazy val root = (project in file("."))
  .aggregate(datatypes, endpoints, logic, server)
  .settings(
    name := "tapir-repro",
    // opt out of aggregation of tasks we need to wire only in root
    baseDirectory / aggregate := false,
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true,
  )
