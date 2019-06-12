package simulator.tests

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import scala.concurrent.duration._
import akka.stream.ActorMaterializer
import akka.testkit.{TestActor, TestProbe}
import akka.util.Timeout
import simulator.actors.HttpService
import simulator.model.actions.system.AddCustomers
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
      val arrears = Scalar()
      val satisfaction = Scalar()
      val customerGenConfig =
        CustomerConfig(id = "Terry", arrears = arrears, satisfaction = satisfaction, kind = "customer")
      val config = SimulationConfig(Some(State()), -1, None)
      Post("/simulation", config) ~> route ~> check {

        response.status shouldEqual StatusCodes.OK

        entityAs[SimulationResults] shouldEqual SimulationResults(Map(), Map(), Map())
      }
    }

    "return results when sent a simulation configuration" in {
      val arrears = Scalar()
      val satisfaction = Scalar()
      val customerGenConfig =
        CustomerConfig(id = "Terry", arrears = arrears, satisfaction = satisfaction, kind = "customer")
      val addCustomerAction: SystemAction =
        AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = Some(Repeat(10, 100)), kind = "addCustomers")
      val initQueue = Map("0" -> List(addCustomerAction))
      val initState: State =
        State(systemActions = initQueue)
      val simConfig = SimulationConfig(Some(initState), -1, None)
      val cusConfig = CustomerConfig()
      val startingState = State(configs = Configurations(cusConfig, simConfig))
      Post("/simulation", startingState) ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
      }
    }

  }
}
