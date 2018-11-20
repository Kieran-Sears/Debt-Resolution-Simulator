package simulator.tests

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import spray.json._
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import simulator.actors.HttpService
import simulator.model.Actions.{Action, AddCustomers, Repeat}
import simulator.model._

class HttpServiceSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with HttpService
    with MarshallingImplicits {

  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val timeout: Timeout = Timeout(5 seconds)

  private val todoRegistryProbe = TestProbe()
  todoRegistryProbe.setAutoPilot((sender: ActorRef, _: Any) => {
    sender ! StatusCodes.OK
    TestActor.KeepRunning
  })

//  "HttpService" should {
//    "return a 200 when sent OPTIONS" in {
//      Options() ~> route ~> check {
//        response.status shouldEqual StatusCodes.OK
//      }
//    }

//  "inform the API caller when an initial action has not been set" in {
//    val customerGenConfig =
//      CustomerGenConfig(10d, DebtTimeVariance.None, 0d, 100)
//    val config = SimulationConfig(State(), -1, None, customerGenConfig)
//    Post("/simulation", config) ~> route ~> check {
//
//      response.status shouldEqual StatusCodes.OK
//
//      entityAs[SimulationResults] shouldEqual SimulationResults(Map(),
//                                                                Map(),
//                                                                Map())
//    }
//  }

  "return results when sent a simulation configuration" in {
    val customerGenConfig =
      CustomerGenConfig(10d, DebtTimeVariance.None, 0d, 100)
    val addCustomerAction: Action =
      AddCustomers(UUID.randomUUID(), Some(Repeat(10, 100)), customerGenConfig)
    val initActionQueue: ActionQueue = ActionQueue(queue = scala.collection.immutable.Map("0" -> List(addCustomerAction)))
    val initState: State =
      State(actionQueue = initActionQueue)
    val config = SimulationConfig(initState, -1, None, customerGenConfig)

    println("config : " + config)

    val payload = config.toJson

    println("payload : " + payload)

    Post("/simulation", payload) ~> route ~> check {
      println("Some Response: " + response)
      response.status shouldEqual StatusCodes.OK
    }
  }

}
