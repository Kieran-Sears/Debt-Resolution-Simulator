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

class HttpServiceSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with HttpService {

  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val timeout: Timeout = Timeout(5 seconds)

  private val todoRegistryProbe = TestProbe()
  todoRegistryProbe.setAutoPilot((sender: ActorRef, _: Any) => {
    sender ! StatusCodes.OK
    TestActor.KeepRunning
  })

  "HttpService" should {
    "return a 200 when sent OPTIONS with valid auth" in {
      Options() ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
      }
    }
  }
}
