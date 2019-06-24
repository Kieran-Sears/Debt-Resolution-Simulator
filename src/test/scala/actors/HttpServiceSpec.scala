package actors

import java.util.UUID

import akka.http.scaladsl.model._
import org.scalatest._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import cats.effect.IO
import org.scalatest.concurrent.ScalaFutures
import simulator.actors.HttpService
import simulator.db.{StorageController, StorageError, StorageImpl}
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

  def FakeStorage(results: Either[StorageError, Configurations]) = new StorageImpl() {

    override def initialiseStorageTables(): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
    override def initialiseTrainingTables(): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
    override def initialisePlayTables(attributes: List[AttributeConfig]): IO[Either[StorageError, Unit]] =
      IO.pure(Right(Unit))
    override def storeConfiguration(username: String, config: Configurations): IO[Either[StorageError, Unit]] =
      IO.pure(Right(Unit))
    override def getConfiguration(configId: UUID): IO[Either[StorageError, Configurations]] = IO.pure(results)
    override def storeTrainingData(data: TrainingData): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
    override def storePlayingData(
      attributes: List[AttributeConfig],
      data: List[(simulator.model.Customer, simulator.model.Action)],
      configurationId: UUID): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
  }

  def callServiceConfigure(implicit store: StorageController): Route = {
    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
    implicit lazy val timeout: Timeout = Timeout(5 seconds)
    val service = new HttpService
    service.configure("TestUsername")
  }

//  it should "reply with a 403 when it receives a PUT request with an invalid payload" in {
//    implicit val ws: WriteService[IO] = FakeWriteService(Left(Unauthorised))
//    val putRoute = new PutMessageRoute[IO]
//
//    val toId = UUID.randomUUID()
//    val fromId = UUID.randomUUID()
//    val req = Request[IO](
//      method = Method.PUT,
//      headers = pu.headerBuilder(Permissions(Some(fromId.toString))),
//      uri = Uri.fromString("/messages/" + UUID.randomUUID()).right.get
//    ).withEntity(Map("to" -> s"$toId", "from" -> s"$fromId").asJson.toString()) // text field missing
//    val resp = putRoute.route(req).unsafeRunSync()
//    resp.status.code should be(403)
//  }

  "POST to Configure route with a valid configuration" should "return a status code 200 with testing examples" in {

    val arrears1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 50, 500)
    val satisfaction1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50)
    val age1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 18, 25)
    val income1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 15000, 22000)

    val arrears2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 500, 2000)
    val satisfaction2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 30, 80)
    val age2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 25, 40)
    val income2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 21000, 40000)

    val arrears3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 5000)
    val satisfaction3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 0, 100)
    val age3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 35, 55)
    val income3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 30000, 50000)

    val rent = OptionConfig(UUID.randomUUID(), "Rent", 50)
    val homeowner = OptionConfig(UUID.randomUUID(), "Homeowner", 20)
    val councilHousing = OptionConfig(UUID.randomUUID(), "Council housing", 30)
    val tenure = CategoricalConfig(UUID.randomUUID(), List(rent.id, homeowner.id, councilHousing.id))

    val arrearsG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50000)
    val satisfactionG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 0, 100)
    val ageG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 18, 85)
    val incomeG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 15000, 22000)

    val rentG = OptionConfig(UUID.randomUUID(), "Rent", 50)
    val homeownerG = OptionConfig(UUID.randomUUID(), "Homeowner", 20)
    val councilHousingG = OptionConfig(UUID.randomUUID(), "Council housing", 30)
    val emergencyG = OptionConfig(UUID.randomUUID(), "Emergency", 30)
    val tenureG =
      CategoricalConfig(id = UUID.randomUUID(), List(rentG.id, homeownerG.id, councilHousingG.id, emergencyG.id))

    val arrearsAttG =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrearsG.id, AttributeEnum.Global)
    val satisfactionAttG =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfactionG.id, AttributeEnum.Global)
    val ageAttG = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = ageG.id, AttributeEnum.Global)
    val incomeAttG = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = incomeG.id, AttributeEnum.Global)
    val tenureAttG = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenureG.id, AttributeEnum.Global)

    val arrearsAtt1 =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears1.id, AttributeEnum.Override)
    val satisfactionAtt1 =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction1.id, AttributeEnum.Override)
    val ageAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age1.id, AttributeEnum.Override)
    val incomeAtt1 =
      AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income1.id, AttributeEnum.Override)
    val tenureAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenure.id, AttributeEnum.Override)

    val arrearsAtt2 =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears2.id, AttributeEnum.Override)
    val satisfactionAtt2 =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction2.id, AttributeEnum.Override)
    val ageAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age2.id, AttributeEnum.Override)
    val incomeAtt2 =
      AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income2.id, AttributeEnum.Override)

    val arrearsAtt3 =
      AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears3.id, AttributeEnum.Override)
    val satisfactionAtt3 =
      AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction3.id, AttributeEnum.Override)
    val ageAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age3.id, AttributeEnum.Override)
    val incomeAtt3 =
      AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income3.id, AttributeEnum.Override)

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

    val scalarConfigs = List(
      arrearsG,
      satisfactionG,
      ageG,
      incomeG,
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

    val effect1 = EffectConfig(UUID.randomUUID(), "ZeroArrears", EffectEnum.Effect, "Arrears")
    val effect2 = EffectConfig(UUID.randomUUID(), "Satisfy", EffectEnum.Effect, "Satisfaction")
    val effect3 = EffectConfig(UUID.randomUUID(), "Cooperative", EffectEnum.Affect, "Arrears")
    val effect4 = EffectConfig(UUID.randomUUID(), "Dissatisfy", EffectEnum.Effect, "Satisfaction")

    val action1 =
      ActionConfig(UUID.randomUUID(), "PayInFull", ActionEnum.Customer, List(effect1.id, effect2.id, effect3.id))
    val action2 = ActionConfig(UUID.randomUUID(), "Litigate", ActionEnum.Agent, List(effect1.id, effect4.id))

    val effectConfigs = List(effect1, effect2, effect3, effect4)
    val actionConfigs = List(action1, action2)
    val categoricalConfigs = List(tenure, tenureG)
    val optionConfigs = List[OptionConfig](rent, homeowner, councilHousing)
    val customerConfigs = List(customerConfig1, customerConfig2, customerConfig3)
    val simulationConfig = SimulationConfig(UUID.randomUUID(), 0, Some(100), 120)

    val attributeConfigs = List(
      ageAttG,
      incomeAttG,
      tenureAttG,
      arrearsAttG,
      satisfactionAttG,
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

    val configurations = Configurations(
      UUID.randomUUID(),
      customerConfigs,
      actionConfigs,
      effectConfigs,
      attributeConfigs,
      scalarConfigs,
      categoricalConfigs,
      optionConfigs,
      simulationConfig
    )

    val dbRet = Right(configurations)
    val fakeStore = FakeStorage(dbRet)

    Put("/configure", configurations) ~> callServiceConfigure(fakeStore) ~> check {
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
