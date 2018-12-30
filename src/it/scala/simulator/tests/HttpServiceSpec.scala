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
import simulator.model.actions.system.AddCustomers
import simulator.model.actions.{Repeat, SystemAction}
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

  "HttpService" should {
    "return a 200 when sent OPTIONS" in {
      Options() ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
      }
    }

    "inform the API caller when an initial action has not been set" in {
      val customerGenConfig =
        CustomerGenConfig(debtVarianceOverTime = DebtTimeVariance.None, kind = "customer")
      val config = SimulationConfig(Some(State()), -1, None)
      Post("/simulation", config) ~> route ~> check {

        response.status shouldEqual StatusCodes.OK

        entityAs[SimulationResults] shouldEqual SimulationResults(Map(), Map(), Map())
      }
    }

    "return results when sent a simulation configuration" in {
      val customerGenConfig =
        CustomerGenConfig(DebtTimeVariance.None, 0d)
      val addCustomerAction: SystemAction =
        AddCustomers(numberOfCustomers = 1, startingDebt = 10, repeat = Some(Repeat(10, 100)), kind = "addCustomers")
      val initQueue = Map("0" -> List(addCustomerAction))
      val initState: State =
        State(systemActions = initQueue)
      val simConfig = SimulationConfig(Some(initState), -1, None)
      val cusConfig = CustomerGenConfig()
      val startingState = State(configs = Configurations(cusConfig, simConfig))
      Post("/simulation", startingState) ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
      }
    }

  }
}

