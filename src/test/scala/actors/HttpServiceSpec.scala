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
import spray.json._

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

  def callServiceConfigure: Route = {
    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
    implicit lazy val timeout: Timeout = Timeout(5 seconds)
    val service = new HttpService
    service.configure
  }
//
//  "POST to Train route with a valid configuration" should "return a status code 200 with testing examples" in {
//    val arrears =
//      AttributeConfig("arrears", Scalar(start = 0, variance = Variance.None, min = 10d, max = 100d))
//    val customerConfig = CustomerConfig(id = "Peter Payer", attributeConfigurations = List("Arrears"))
//    val configs = Configurations(List(customerConfig))
//
//    Post("/train", configs) ~> callServiceTrain ~> check {
//      status shouldEqual StatusCodes.OK
//      entityAs[List[Customer]].map(customer => customer.arrears shouldEqual 55.0 +- 45.0)
//    }
//  }

  "POST to Configure route with a valid configuration" should "return a status code 200 with testing examples" in {

    val configs =
      """{
        |  "simulationConfiguration": {
        |    "startTime": 1,
        |    "endTime": 100,
        |    "numberOfCustomers": 50,
        |    "id": "Configuration1",
        |    "kind": "simulation"
        |  },
        |  "customerConfigurations": [
        |    {
        |      "proportion": 20,
        |      "arrears": {
        |        "min": 50,
        |        "max": 1000,
        |        "variance": "None",
        |        "id": "aff42387-1b7b-4cf6-ac52-8f59233f7c15",
        |        "kind": "scalar"
        |      },
        |      "satisfaction": {
        |        "min": 0,
        |        "max": 100,
        |        "variance": "None",
        |        "id": "3d7b5293-989d-4aad-b821-f1957b4eaa3b",
        |        "kind": "scalar"
        |      },
        |      "attributeConfigurations": [
        |        "Age",
        |        "Income",
        |        "Tenure"
        |      ],
        |      "id": "Maggiepayer",
        |      "kind": "customer"
        |    },
        |    {
        |      "proportion": 20,
        |      "arrears": {
        |        "min": 50,
        |        "max": 1000,
        |        "variance": "None",
        |        "id": "3eeaeb27-4056-4a53-a1b5-aafb363beeba",
        |        "kind": "scalar"
        |      },
        |      "satisfaction": {
        |        "min": 0,
        |        "max": 100,
        |        "variance": "None",
        |        "id": "45be24bc-872b-4c0f-9a3c-e278d8a554e2",
        |        "kind": "scalar"
        |      },
        |      "attributeConfigurations": [
        |        "GrumpyAge",
        |        "Hair Colour"
        |      ],
        |      "id": "Grumpyboris",
        |      "kind": "customer"
        |    }
        |  ],
        |  "actionConfigurations": [
        |    {
        |      "effectConfigurations": [
        |        "ZeroArrears",
        |        "MakeHappy",
        |        "WantToPay",
        |        "NotTooSkint",
        |        "NotAngryAtCompany"
        |      ],
        |      "id": "Payinfull",
        |      "kind": "action"
        |    },
        |    {
        |      "effectConfigurations": [
        |        "ZeroArrears",
        |        "AnnoyHighly",
        |        "HighArrears"
        |      ],
        |      "id": "Litigate",
        |      "kind": "action"
        |    }
        |  ],
        |  "effectConfigurations": [
        |    {
        |      "type": "Effect",
        |      "target": "Arrears",
        |      "id": "Zeroarrears",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Effect",
        |      "target": "Satisfaction",
        |      "id": "Makehappy",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Affect",
        |      "target": "Satisfaction",
        |      "id": "Wanttopay",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Affect",
        |      "target": "Income",
        |      "id": "Nottooskint",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Affect",
        |      "target": "Satisfaction",
        |      "id": "Notangryatcompany",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Effect",
        |      "target": "Satisfaction",
        |      "id": "Annoyhighly",
        |      "kind": "effects"
        |    },
        |    {
        |      "type": "Affect",
        |      "target": "Arrears",
        |      "id": "Higharrears",
        |      "kind": "effects"
        |    }
        |  ],
        |  "attributeConfigurations": [
        |    {
        |      "value": {
        |        "min": 50,
        |        "max": 85,
        |        "variance": "None",
        |        "id": "7425df2d-da4f-445a-b436-db83cc76afee",
        |        "kind": "scalar"
        |      },
        |      "id": "Age",
        |      "kind": "attribute"
        |    },
        |    {
        |      "value": {
        |        "min": 12000,
        |        "max": 18000,
        |        "variance": "None",
        |        "id": "2feaccb2-dfc7-4f70-a371-70adffc2f50d",
        |        "kind": "scalar"
        |      },
        |      "id": "Income",
        |      "kind": "attribute"
        |    },
        |    {
        |      "value": {
        |        "options": [
        |          "Homeowner",
        |          "Renting",
        |          "Council Housing"
        |        ],
        |        "id": "958b2cb7-d876-417e-a466-c6218c6a384b",
        |        "kind": "categorical"
        |      },
        |      "id": "Tenure",
        |      "kind": "attribute"
        |    },
        |    {
        |      "value": {
        |        "min": 70,
        |        "max": 85,
        |        "variance": "None",
        |        "id": "f6281d86-442e-4f71-9923-9f7549cdb995",
        |        "kind": "scalar"
        |      },
        |      "id": "Grumpyage",
        |      "kind": "attribute"
        |    },
        |    {
        |      "value": {
        |        "options": [
        |          "Red",
        |          "Blonde",
        |          "Brown"
        |        ],
        |        "id": "dacca39b-9add-4702-847b-7003ff746046",
        |        "kind": "categorical"
        |      },
        |      "id": "Hair colour",
        |      "kind": "attribute"
        |    }
        |  ],
        |  "optionConfigurations": [
        |    {
        |      "probability": 10,
        |      "id": "Homeowner",
        |      "kind": "categoricalOption"
        |    },
        |    {
        |      "probability": 80,
        |      "id": "Renting",
        |      "kind": "categoricalOption"
        |    },
        |    {
        |      "probability": 10,
        |      "id": "Council housing",
        |      "kind": "categoricalOption"
        |    },
        |    {
        |      "probability": 10,
        |      "id": "Red",
        |      "kind": "categoricalOption"
        |    },
        |    {
        |      "probability": 40,
        |      "id": "Blonde",
        |      "kind": "categoricalOption"
        |    },
        |    {
        |      "probability": 50,
        |      "id": "Brown",
        |      "kind": "categoricalOption"
        |    }
        |  ],
        |  "id": "559670e2-96ae-49f5-9a1a-ef8afe0abb3c",
        |  "kind": "configuration"
        |}""".stripMargin.parseJson

    Put("/configure", configs) ~> callServiceConfigure ~> check {
      status shouldEqual StatusCodes.OK
      entityAs[List[Customer]].map(customer => customer.arrears shouldEqual 525.0 +- 475.0)
    }
  }

}
