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

import scala.util.{Failure, Success}

trait HttpService extends MarshallingImplicits {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val interfaceA = "localhost"
  val portA = 8080

  val stateActor =
    system.actorOf(Props(classOf[StateActor]),
                   "stateActor_" + UUID.randomUUID())

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
    pathPrefix("queue") {
      post {
        entity(as[SimulationConfig]) { conf =>
          {
            // todo example of what is needed to kick off simulation

//            startState: State,
//            timeFrom: Int,
//            timeTill: Int,
//            customerGenParams: CustomerGenConfig

            stateActor ! UpdateState(conf.startState)
            stateActor ! TickOnTime(conf.startTime, 0, conf.endTime)

            onComplete(
              (stateActor ? RunSimulation(conf)).mapTo[State]) {
              case Success(state) => {
                val results = getStats(state)
                complete(results)
              }
              case Failure(error) => complete(error)
            }
          }
        }
      }
    }
  }

  def getStats(currentState: State) = {
    SimulationResults(
      batches =
        currentState.history
          .foldLeft[Map[Int, Double]](Map())((acc, state: State) =>
            acc ++ Map(state.time -> state.stats.batchArrears)),
      totals =
        currentState.history
          .foldLeft[Map[Int, Double]](Map())((acc, state: State) =>
            acc ++ Map(state.time -> state.stats.totalArrears)),
      aging =
        currentState.history
          .map(state => state.time)
          .zip(
            (currentState.history :+ currentState).reverse.map(ts => ts.stats.batchArrears)
          ).toMap
    )
  }

  def timeToString(time: Int) = time.toString + "-" + (time + 10)

}
