package simulator

import java.util.logging.Logger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import simulator.actors.HttpService

import scala.concurrent.duration._

class Main(
    implicit val system: ActorSystem,
    implicit val materializer: ActorMaterializer,
    implicit val timeout: Timeout
) extends HttpService {
  def startServer(): Unit = {
    Http()
      .bindAndHandle(
        handler = route,
        interface = interfaceA,
        port = portA,
        settings = ServerSettings(ConfigFactory.load)
      )
  }
}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("Simulation")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(10 seconds)

  val simulation = new Main()
  simulation.startServer()

}
