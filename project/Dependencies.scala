import sbt.{ModuleID => M, _}
import Dependencies.{Version => V}

object Dependencies {

  sealed trait Version {
    def scalaBinaryVersion: String
    def scalaFullVersion: String
    def akkaHttp: String
    def akka: String
    def akkaStreamContrib: String
    def akkaActorArtifact: String
    def circe: String
    def tapir: Version.OrgVer
    def tapirClient: Version.OrgVer
    def magnolia: String
    def scalacheck: String
  }

  object Version {

    case class OrgVer(org: String, version: String)

    object S211 extends Version {
      override val scalaBinaryVersion           = "2.11"
      override val scalaFullVersion             = "2.11.12"
      override val akkaHttp                     = "10.1.14"
      override val akka                         = "2.5.32"
      override val akkaStreamContrib            = "0.10+9-a20362e2"
      override val akkaActorArtifact            = "akka-actor"
      override val circe                        = "0.12.0-M3"
      override val tapir: OrgVer                = OrgVer("com.softwaremill.tapir", "0.8.11")
      override val tapirClient: OrgVer          = OrgVer("com.softwaremill.sttp.client", "2.2.9")
      override val magnolia                     = "0.10.0"
      override val scalacheck                   = "1.15.2"
    }

    object S212 extends Version {
      override val scalaBinaryVersion           = "2.12"
      override val scalaFullVersion             = "2.12.15"
      override val akkaHttp                     = "10.2.9"
      override val akka                         = "2.6.18"
      override val akkaStreamContrib            = "0.11+4-91b2f9fa"
      override val akkaActorArtifact            = "akka-actor-typed"
      override val circe                        = "0.14.1"
      override val tapir: OrgVer                = OrgVer("com.softwaremill.sttp.tapir", "1.0.0-M1")
      override val tapirClient: OrgVer          = OrgVer("com.softwaremill.sttp.client3", "3.5.1")
      override val magnolia                     = "0.17.0"
      override val scalacheck                   = "1.15.4"
    }

    object S213 extends Version {
      override val scalaBinaryVersion           = "2.13"
      override val scalaFullVersion             = "2.13.8"
      override val akkaHttp                     = "10.2.9"
      override val akka                         = "2.6.18"
      override val akkaStreamContrib            = "0.11+4-91b2f9fa"
      override val akkaActorArtifact            = "akka-actor-typed"
      override val circe                        = "0.14.1"
      override val tapir: OrgVer                = OrgVer("com.softwaremill.sttp.tapir", "1.0.0-M1")
      override val tapirClient: OrgVer          = OrgVer("com.softwaremill.sttp.client3", "3.5.1")
      override val magnolia                     = "0.17.0"
      override val scalacheck                   = "1.15.4"
    }
  }

  // akka
  val akkaHttp:               V => M = v => "com.typesafe.akka" %% "akka-http"                    % v.akkaHttp
  val akkaHttpCore:           V => M = v => "com.typesafe.akka" %% "akka-http-core"               % v.akkaHttp
  val akkaActor:              V => M = v => "com.typesafe.akka" %% v.akkaActorArtifact            % v.akka
  val akkaPersistenceTestkit: V => M = v => "com.typesafe.akka" %% "akka-persistence-testkit"     % v.akka
  val akkaStream:             V => M = v => "com.typesafe.akka" %% "akka-stream"                  % v.akka
  val akkaStreamTestkit:      V => M = v => "com.typesafe.akka" %% "akka-stream-testkit"          % v.akka
  val akkaSlf4j:              V => M = v => "com.typesafe.akka" %% "akka-slf4j"                   % v.akka
  val akkaStreamContrib:      V => M = v => "com.typesafe.akka" %% "akka-stream-contrib"          % v.akkaStreamContrib
  val akkaActorTestkitTyped:  V => M = v => "com.typesafe.akka" %% "akka-actor-testkit-typed"     % v.akka

  val akkaStreamAlpakkaJsonStreaming = "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "3.0.4"

  // akka test
  val akkaHttpTestkit: V => M = v => "com.typesafe.akka" %% "akka-http-testkit" % v.akkaHttp

  // tapir
  val tapirCore:             V => M = v => v.tapir.org       %% "tapir-core"               % v.tapir.version
  val tapirAkkaHttpServer:   V => M = v => v.tapir.org       %% "tapir-akka-http-server"   % v.tapir.version
  val tapirSwaggerUi:        V => M = v => v.tapir.org       %% "tapir-swagger-ui"         % v.tapir.version
  val tapirOpenapiDocs:      V => M = v => v.tapir.org       %% "tapir-openapi-docs"       % v.tapir.version
  val tapirOpenapiCirceYaml: V => M = v => v.tapir.org       %% "tapir-openapi-circe-yaml" % v.tapir.version
  val tapirJsonCirce:        V => M = v => v.tapir.org       %% "tapir-json-circe"         % v.tapir.version
  val tapirSttpClient:       V => M = v => v.tapir.org       %% "tapir-sttp-client"        % v.tapir.version
  val sttp3Akka:             V => M = v => v.tapirClient.org %% "akka-http-backend"        % v.tapirClient.version

  val sttpSharedAkka = "com.softwaremill.sttp.shared" %% "akka" % "1.3.2"

  // circe
  val circeCore:    V => M = v => "io.circe" %% "circe-core"    % v.circe
  val circeGeneric: V => M = v => "io.circe" %% "circe-generic" % v.circe
  val circeParser:  V => M = v => "io.circe" %% "circe-parser"  % v.circe

  // logging
  val logbackVersion = "1.2.10"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logbackCore    = "ch.qos.logback" % "logback-core"    % logbackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
  val logstashLogback = "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1"

  // metrics & events
  val eventLoggerVersion = "0.3.9"
  val eventLoggerFirehoseLogger = "com.sparkbeyond.eventlogging" %% "event-logger-firehose-logger" % eventLoggerVersion
  val eventLoggerEncodeCirce    = "com.sparkbeyond.eventlogging" %% "event-logger-encode-circe"    % eventLoggerVersion

  // util
  val config       = "com.typesafe"       % "config"         % "1.4.2"
  val commonsText  = "org.apache.commons" % "commons-text"   % "1.9"
  val commonsLang3 = "org.apache.commons" % "commons-lang3"  % "3.12.0"
  val scopt        = "com.github.scopt"  %% "scopt"          % "4.0.1"
  val jctools      = "org.jctools"        % "jctools-core"   % "3.3.0"
  val catsCore     = "org.typelevel"     %% "cats-core"      % "2.7.0"

  // test
  val scalacheck:      V => M = v => "org.scalacheck" %% "scalacheck"             % v.scalacheck
  val scalatest                    = "org.scalatest"  %% "scalatest"              % "3.2.11"
  val tapirMockServer: V => M = v => v.tapir.org      %% "tapir-sttp-mock-server" % v.tapir.version

  // postgresql
  val postgresql = "org.postgresql" % "postgresql" % "42.3.3"

  // h2
  val h2Database = "com.h2database" % "h2" % "2.1.210"
}
