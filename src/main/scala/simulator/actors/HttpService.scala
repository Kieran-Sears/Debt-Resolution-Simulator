package simulator.actors

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import simulator.model._

import scala.concurrent.Await

trait HttpService extends MarshallingImplicits {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  lazy val log: LoggingAdapter = Logging(system, classOf[HttpService])

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
        entity(as[State]) { state => { // todo get list of actions player wants to do for this state
          complete("") // todo return current state
        } }
      }
    }
  }

  def train: Route = {
    pathPrefix("train") {
      post {
        entity(as[State]) { state => {
          complete("") // todo return accuracy, loss, etc
        } }
      }
    }
  }

  def test: Route = {
    pathPrefix("simulation") {
      post {
        extractRequest map (request => {
          println("initialise Simulation endpoint request : " + request)
        })

        entity(as[State]) { startState =>
          {

            val stateActor =
              system.actorOf(Props(classOf[StateActor]),
                "stateActor_" + UUID.randomUUID())

            Await.result(stateActor ? RunSimulation(startState), timeout.duration).asInstanceOf[SimulationComplete] match {
                  case results: SimulationResults => complete(results)
                  case error: SimulationError => complete(error.reason)
            }
          }
        }
      }
    }
  }
}
