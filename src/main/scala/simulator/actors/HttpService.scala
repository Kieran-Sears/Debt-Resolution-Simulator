package simulator.actors

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import simulator.model._

import scala.concurrent.Await
import scala.util.{Failure, Success}

trait HttpService extends MarshallingImplicits {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val interfaceA = "localhost"
  val portA = 8080

  println(s"Starting Collaborate http interface at: $interfaceA:$portA")

  val route: Route = {
      options {
        complete(OK)
      } ~
        encodeResponse {
          initialiseSimulation
        }
  }

  def initialiseSimulation: Route = {
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
