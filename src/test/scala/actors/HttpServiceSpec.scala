package actors

import java.util.UUID
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

  def callServiceConfigure: Route = {
    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
    implicit lazy val timeout: Timeout = Timeout(5 seconds)
    val service = new HttpService
    service.configure("TestUsername")
  }

  "POST to Configure route with a valid configuration" should "return a status code 200 with testing examples" in {

    val arrears1 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 50, 500)
    val satisfaction1 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 10, 50)
    val age1 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 18, 25)
    val income1 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 15000, 22000)

    val arrears2 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 500, 2000)
    val satisfaction2 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 30, 80)
    val age2 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 25, 40)
    val income2 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 21000, 40000)

    val arrears3 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 10, 5000)
    val satisfaction3 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 0, 100)
    val age3 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 35, 55)
    val income3 = ScalarConfig(id = UUID.randomUUID(), Variance.None, 30000, 50000)

    val rent = OptionConfig(id = UUID.randomUUID(), "Rent", 50)
    val homeowner = OptionConfig(id = UUID.randomUUID(), "Homeowner", 20)
    val councilHousing = OptionConfig(id = UUID.randomUUID(), "Council housing", 30)
    val tenure = CategoricalConfig(id = UUID.randomUUID(), List(rent.id, homeowner.id, councilHousing.id))

    val arrearsG = ScalarConfig(id = UUID.randomUUID(), Variance.None, 10, 50000)
    val satisfactionG = ScalarConfig(id = UUID.randomUUID(), Variance.None, 0, 100)
    val ageG = ScalarConfig(id = UUID.randomUUID(), Variance.None, 18, 85)
    val incomeG = ScalarConfig(id = UUID.randomUUID(), Variance.None, 15000, 22000)

    val rentG = OptionConfig(id = UUID.randomUUID(), "Rent", 50)
    val homeownerG = OptionConfig(id = UUID.randomUUID(), "Homeowner", 20)
    val councilHousingG = OptionConfig(id = UUID.randomUUID(), "Council housing", 30)
    val emergencyG = OptionConfig(id = UUID.randomUUID(), "Emergency", 30)
    val tenureG =
      CategoricalConfig(id = UUID.randomUUID(), List(rentG.id, homeownerG.id, councilHousingG.id, emergencyG.id))

    val arrearsAttG = AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrearsG.id)
    val satisfactionAttG = AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfactionG.id)
    val ageAttG = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = ageG.id)
    val incomeAttG = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = incomeG.id)
    val tenureAttG = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenureG.id)

    val arrearsAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears1.id)
    val satisfactionAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction1.id)
    val ageAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age1.id)
    val incomeAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income1.id)
    val tenureAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenure.id)

    val arrearsAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears2.id)
    val satisfactionAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction2.id)
    val ageAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age2.id)
    val incomeAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income2.id)

    val arrearsAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears3.id)
    val satisfactionAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction3.id)
    val ageAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age3.id)
    val incomeAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income3.id)

    val customerConfig1 = CustomerConfig(
      id = UUID.randomUUID(),
      name = "LowRoller",
      attributeOverrides = List(arrearsAtt1.id, ageAtt1.id, satisfactionAtt1.id, incomeAtt1.id, tenureAtt1.id),
      proportion = 20
    )

    val customerConfig2 = CustomerConfig(
      id = UUID.randomUUID(),
      name = "MidRoller",
      attributeOverrides = List(arrearsAtt2.id, ageAtt2.id, satisfactionAtt2.id, incomeAtt2.id),
      proportion = 20
    )

    val customerConfig3 = CustomerConfig(
      id = UUID.randomUUID(),
      name = "HighRoller",
      attributeOverrides = List(arrearsAtt3.id, ageAtt3.id, satisfactionAtt3.id, incomeAtt3.id),
      proportion = 20
    )
    val overrideConfigs = List(
      ageAtt1,
      incomeAtt1,
      tenureAtt1,
      arrearsAtt1,
      satisfactionAtt1,
      ageAtt2,
      incomeAtt2,
      arrearsAtt2,
      satisfactionAtt2,
      ageAtt3,
      incomeAtt3,
      arrearsAtt3,
      satisfactionAtt3
    )

    val scalarConfigs = List(
      arrears1,
      satisfaction1,
      age1,
      income1,
      arrears2,
      satisfaction2,
      age2,
      income2,
      arrears3,
      satisfaction3,
      age3,
      income3)

    val effect1 = EffectConfig(UUID.randomUUID(), "ZeroArrears", EffectType.Effect, "Arrears")
    val effect2 = EffectConfig(UUID.randomUUID(), "Satisfy", EffectType.Effect, "Satisfaction")
    val effect3 = EffectConfig(UUID.randomUUID(), "Cooperative", EffectType.Affect, "Arrears")
    val effect4 = EffectConfig(UUID.randomUUID(), "Dissatisfy", EffectType.Effect, "Satisfaction")

    val action1 =
      ActionConfig(UUID.randomUUID(), "PayInFull", ActionType.Customer, List(effect1.id, effect2.id, effect3.id))
    val action2 = ActionConfig(UUID.randomUUID(), "Litigate", ActionType.Agent, List(effect1.id, effect4.id))

    val effectConfigs = List(effect1, effect2, effect3, effect4)
    val actionConfigs = List(action1, action2)
    val categoricalConfigs = List(tenure)
    val optionConfigs = List[OptionConfig](rent, homeowner, councilHousing)
    val customerConfigs = List(customerConfig1, customerConfig2, customerConfig3)
    val simulationConfig = SimulationConfig(UUID.randomUUID(), 0, Some(100), 120)

    val attributeConfigs = List(
      ageAttG,
      incomeAttG,
      tenureAttG,
      arrearsAttG,
      satisfactionAttG
    )

    val configurations = Configurations(
      UUID.randomUUID(),
      customerConfigs,
      actionConfigs,
      effectConfigs,
      attributeConfigs,
      overrideConfigs,
      scalarConfigs,
      categoricalConfigs,
      optionConfigs,
      Nil,
      simulationConfig
    )

    Put("/configure", configurations) ~> callServiceConfigure ~> check {
      status shouldEqual StatusCodes.OK
      val data = entityAs[TrainingData]
      data.customers.map(customer => {
        if (customer.name == "LowRoller")
          customer.getArrears.value shouldEqual 275.0 +- 225.0
        if (customer.name == "MidRoller")
          customer.getArrears.value shouldEqual 1250.0 +- 750.0
        if (customer.name == "HighRoller")
          customer.getArrears.value shouldEqual 2495.0 +- 2505.0
      })
    }
  }

}
