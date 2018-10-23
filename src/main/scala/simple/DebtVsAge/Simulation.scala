package simple.DebtVsAge

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import simple.DebtVsAge.AkkaInterface.StateActor
import simple.DebtVsAge.model.{State, TickOnTime, UpdateState}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Simulation {

  implicit val system: ActorSystem = ActorSystem("Simulation")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10 seconds)

  def apply(state: State = State()) = {
    val id = UUID.randomUUID()
    val stateActor =
      system.actorOf(Props(classOf[StateActor]), "stateActor_" + id)
    stateActor ! UpdateState(state)
    stateActor ! TickOnTime(-1, 0)
  }

}
