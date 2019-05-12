package simulator.actors

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.HttpRequest
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import simulator.Generator
import simulator.model._

import scala.concurrent.Await
import scala.util.{Failure, Success}

class HttpService(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val timeout: Timeout
) extends MarshallingImplicits {

  lazy val log: LoggingAdapter = Logging(system, classOf[HttpService])

  lazy val gen: Generator = Generator.default

  val interfaceA = "localhost"
  val portA = 8080

  println(s"Starting Collaborate http interface at: $interfaceA:$portA")

  val route: Route = {
    options {
      complete(OK)
    } ~
    encodeResponse {
      play ~
      test ~
      train
    }
  }

  def play: Route = {
    pathPrefix("play") {
      post {
        entity(as[State]) { state =>
          { // todo get list of actions player wants to do for this state
            complete("") // todo return current state
          }
        }
      }
    }
  }

  def train: Route = {
    pathPrefix("train") {
      put {
        entity(as[Configurations]) { configs =>
          {
            val (customers, state) = gen.actualiseConfigs(configs)
            customers match {
              case Success(c) => complete(OK, c)
              case Failure(e) => complete(InternalServerError, e.getMessage)
            }
          }
        }
      }
    }
  }

  def test: Route = {
    pathPrefix("test") {
      post {
        extractRequest map (request => {
          println("initialise Simulation endpoint request : " + request)
        })

        entity(as[State]) { startState =>
          {

            val stateActor =
              system.actorOf(Props(classOf[StateActor]), "stateActor_" + UUID.randomUUID())

            Await
              .result(stateActor ? RunSimulation(startState), timeout.duration)
              .asInstanceOf[SimulationComplete] match {
              case results: SimulationResults => complete(results)
              case error: SimulationError => complete(error.reason)
            }
          }
        }
      }
    }
  }
}
