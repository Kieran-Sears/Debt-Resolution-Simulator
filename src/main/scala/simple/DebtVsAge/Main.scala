package simple.DebtVsAge

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import simple.DebtVsAge.AkkaInterface.HttpAPIActor
import scala.concurrent.duration._
import simple.DebtVsAge.model.Actions.{AddCustomers, Repeat}
import simple.DebtVsAge.model.{ActionQueue, CustomerGenConfig, State}

import scala.concurrent.ExecutionContext

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("Simulation")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10 seconds)

  val id = UUID.randomUUID()
  val stateActor =
    system.actorOf(Props(classOf[HttpAPIActor]), "HttpApiActor_" + id)

//  val customerGenerationParameters =
//    CustomerGeneratorParameters(10, DebtTimeVariance.None, 1, 10)
//
//  val initQueue = ActionQueue
//    .addNewAction(0,
//                  AddCustomers(UUID.randomUUID(), Some(Repeat(10, 100))),
//                  Map())
//
//  val snapshotInterval = 50
//
//  Simulation(State(actionQueue = initQueue))

}


