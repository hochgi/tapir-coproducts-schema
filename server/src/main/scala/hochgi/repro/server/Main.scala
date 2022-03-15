package hochgi.repro.server

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.actor.typed.{Behavior, Scheduler, Terminated}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import hochgi.repro.datatypes.{ManagedError, XConf}
import hochgi.repro.endpoints.Base
import hochgi.repro.logic.GetXConf
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.{SwaggerUI, SwaggerUIOptions}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

object Main extends App with LazyLogging {

  def undocumentedRoutes(context:              ActorContext[NotUsed])
                        (implicit actorSystem: ClassicActorSystem,
                         scheduler:            Scheduler,
                         ec:                   ExecutionContext): Route  = get {
    path("kill") {
      complete {
        scheduler.scheduleOnce(1.milli, new Runnable {
          override def run(): Unit = {
            actorSystem.terminate().transform {
              case Success(_) => Try(context.system.terminate())
              case Failure(e) => Try(context.system.terminate()).transform(_ => Failure(e), { t =>
                t.addSuppressed(e)
                Failure(t)
              })
            }.onComplete {
              case Success(_) => logger.info("Server killed!")
              case Failure(e) => logger.error("Server assassination failed!", e)
            }
          }
        })
        HttpResponse(status = StatusCodes.OK, entity = "Bye bye!")
      }
    }
  }


  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>

    implicit val classicActorSystem: ClassicActorSystem = context.system.classicSystem
    implicit val execContext: ExecutionContext = classicActorSystem.dispatcher
    implicit val scheduler: Scheduler = context.system.scheduler

    val openApiDocs: OpenAPI = OpenAPIDocsInterpreter().toOpenAPI(
      es = List(Base.getX),
      title = "FooBar API",
      version = "0.1.0")

    val docsAsYaml:                                String = openApiDocs.toYaml
    val uiOptions:                       SwaggerUIOptions = SwaggerUIOptions.default.copy(pathPrefix = List("doc"))
    val swaggerUIEndPs: List[ServerEndpoint[Any, Future]] = SwaggerUI[Future](docsAsYaml, uiOptions)
    val full:                 ServerEndpoint[Any, Future] = Base.getX.serverLogicPure(GetXConf.get)
    val swaggerUIRoute:                             Route = AkkaHttpServerInterpreter().toRoute(full :: swaggerUIEndPs)
    val undocumented:                               Route = undocumentedRoutes(context)
    val allRoutes:                                  Route = concat(swaggerUIRoute, undocumented)
    val bindingFuture = Http()
      .newServerAt("localhost", 8506)
      .bind(allRoutes)

    Behaviors.receiveSignal {
      case (_, Terminated(_)) =>
        bindingFuture.foreach(_.terminate(10.seconds))
        Behaviors.stopped[NotUsed]
    }
  }

  akka.actor.typed.ActorSystem(Main(), "blueocean")
}
