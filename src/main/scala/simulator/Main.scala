package simulator

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import cats.effect.{ContextShift, IO}
import com.typesafe.config.ConfigFactory
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts
import simulator.actors.HttpService
import simulator.db.ConfigurationStorage
import simulator.db.configuration._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Main(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val timeout: Timeout
) {

  val httpService: HttpService = new HttpService()

  val clientRouteLogged = DebuggingDirectives.logRequestResult("Client ReST", Logging.InfoLevel)(httpService.route)
  Http()
    .bindAndHandle(
      handler = clientRouteLogged,
      interface = httpService.interfaceA,
      port = httpService.portA,
      settings = ServerSettings(ConfigFactory.load)
    )
}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("Simulation")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(10 seconds)

  ConfigurationStorage.initialiseTables()

  val simulation = new Main()
}
