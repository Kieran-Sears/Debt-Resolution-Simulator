package simple.DebtVsAge.AkkaInterface

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.http.scaladsl.server.{AuthorizationFailedRejection, RejectionHandler, Route}
import simple.DebtVsAge.model._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class HttpAPIActor extends Actor with ActorLogging with MarshallingImplicits {

  implicit val system: ActorSystem = context.system

  implicit lazy val ec: ExecutionContext = context.dispatcher

  implicit lazy val timeout: Timeout = Timeout(5 seconds)

  val stateActor =
    system.actorOf(Props(classOf[StateActor]),
                   "stateActor_" + UUID.randomUUID())



  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          // clause in here for excluding OPTIONS calls
          complete(
            (Unauthorized,
             "The supplied authentication is not authorized to access this resource"))
      }
      .result()

  val route: Route = {
    options {
      complete(OK)
    } ~
      encodeResponse {
        initialiseSimulation
      }
  }

  def receive: Receive = {
    case m @ _ => log.debug("Http API received unexpected msg: {}", m)
  }

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
          .foldLeft[List[(Int, Double)]](Nil)((acc, state: State) =>
            acc :+ (state.time, state.stats.batchArrears)),
      totals =
        currentState.history
          .foldLeft[List[(Int, Double)]](Nil)((acc, state: State) =>
            acc :+ (state.time, state.stats.totalArrears)),
      aging =
        currentState.history
          .map(state => state.time)
          .zip((currentState.history :+ currentState).reverse
            .map(ts => ts.stats.batchArrears))
    )
  }

  def timeToString(time: Int) = time.toString + "-" + (time + 10)

}

case class SimulationResults(batches: List[(Int, Double)], totals: List[(Int, Double)], aging: List[(Int, Double)])