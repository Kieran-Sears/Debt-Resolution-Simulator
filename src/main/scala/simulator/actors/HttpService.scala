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

//  implicit def rejectionHandler: RejectionHandler =
//    RejectionHandler
//      .newBuilder()
//      .handle {
//        case AuthorizationFailedRejection =>
//          // clause in here for excluding OPTIONS calls
//          complete(
//            (Unauthorized,
//             "The supplied authentication is not authorized to access this resource"))
//      }
//      .result()

  val route: Route = {
      options {
        complete(OK)
      } ~
        encodeResponse {
          initialiseSimulation
        }
  }

  println(s"Starting Collaborate http interface at: $interfaceA:$portA")


  def initialiseSimulation: Route = {
    pathPrefix("simulation") {
      post {
        extractRequest map (request => {
          println("initialise Simulation endpoint request : " + request)
        })

        entity(as[SimulationConfig]) { conf =>
          {
            val stateActor =
              system.actorOf(Props(classOf[StateActor]),
                "stateActor_" + UUID.randomUUID())

            Await.result(stateActor ? RunSimulation(conf), timeout.duration).asInstanceOf[SimulationComplete] match {
                  case results: SimulationResults => complete(results)
                  case error: SimulationError => complete(error.reason)
            }
          }
        }
      }
    }
  }
}
