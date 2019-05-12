package actors

import akka.http.scaladsl.model._
import org.scalatest._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import simulator.actors.HttpService
import simulator.model._
import scala.concurrent.duration._

class HttpServiceSpec
  extends FlatSpec
  with Matchers
  with ScalatestRouteTest
  with TestKitBase
  with ImplicitSender
  with MarshallingImplicits
  with TryValues
  with ScalaFutures {

  def callServiceTrain: Route = {
    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
    implicit lazy val timeout: Timeout = Timeout(5 seconds)
    val service = new HttpService
    service.train
  }

  "POST to Train route with a valid configuration" should "return a status code 200 with testing examples" in {
    val arrears =
      Attribute("arrears", Scalar(start = 0, variance = Variance.None, min = 10d, max = 100d))
    val customerConfig = CustomerConfig(id = "Peter Payer", attributeConfigurations = List(arrears), assignedLabel = 0)
    val configs = Configurations(List(customerConfig))

    Post("/train", configs) ~> callServiceTrain ~> check {
      status shouldEqual StatusCodes.OK
      entityAs[List[Customer]].map(customer => customer.arrears shouldEqual 55.0 +- 45.0)
    }
  }
}
