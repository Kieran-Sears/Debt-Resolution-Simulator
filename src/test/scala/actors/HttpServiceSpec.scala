//package actors
//
//import java.util.UUID
//
//import akka.http.scaladsl.model._
//import org.scalatest._
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import akka.stream.ActorMaterializer
//import akka.testkit.{ImplicitSender, TestKitBase}
//import akka.util.Timeout
//import cats.effect.IO
//import simulator.actors.HttpService
//import simulator.classifier.{ArtificialNeuralNetwork, DataLoader, KerasModel, SparkDataLoader}
//import simulator.db.{StorageController, StorageError, StorageImpl}
//import simulator.model._
//import util.MockData
//
//import scala.concurrent.duration._
//
//class HttpServiceSpec
//  extends FlatSpec
//  with Matchers
//  with ScalatestRouteTest
//  with TestKitBase
//  with ImplicitSender
//  with MarshallingImplicits {
//
//  val mockData = new MockData()
//
//  def FakeStorage(results: Either[StorageError, Configurations]) = new StorageImpl() {
//
//    override def initialiseStorageTables(): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
//    override def initialiseTrainingTables(): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
//    override def initialisePlayTables(attributes: List[AttributeConfig]): IO[Either[StorageError, Unit]] =
//      IO.pure(Right(Unit))
//    override def storeConfiguration(username: String, config: Configurations): IO[Either[StorageError, Unit]] =
//      IO.pure(Right(Unit))
//    override def getConfiguration(configId: UUID): IO[Either[StorageError, Configurations]] = IO.pure(results)
//    override def storeTrainingData(data: TrainingData): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
//    override def storePlayingData(
//      attributes: List[AttributeConfig],
//      data: List[(simulator.model.Customer, simulator.model.Action)],
//      configurationId: UUID,
//      labels: Map[String, Int]): IO[Either[StorageError, Unit]] = IO.pure(Right(Unit))
//  }
//
//  def FakeDataLoader(results: Either[StorageError, Configurations]) = new SparkDataLoader() {}
//
//  def FakeArtificialNeuralNetwork(results: Either[StorageError, Configurations]) = new KerasModel() {}
//
//  def callServiceConfigure(implicit store: StorageController, dl: DataLoader, ann: ArtificialNeuralNetwork) = {
//    implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
//    implicit lazy val timeout: Timeout = Timeout(5 seconds)
//    new HttpService
//  }
//
//  "HttpService" should "return 200 OK with testing examples when given a valid configuration to Configure PUT route" in {
//
//    val configurations = mockData.getValidConfiguration()
//    val dbRet = Right(configurations)
//    val fakeStore = FakeStorage(dbRet)
//    val fakeDl = FakeDataLoader()
//    val fakeAnn = FakeArtificialNeuralNetwork()
//
//    Put("/configure", configurations) ~> callServiceConfigure(fakeStore, fakeDl, fakeAnn).configure("TestUsername") ~> check {
//      status shouldEqual StatusCodes.OK
//      val data = entityAs[TrainingData]
//      data.customers.map(customer => {
//        if (customer.name == "LowRoller")
//          customer.getArrears.value shouldEqual 275.0 +- 225.0
//        if (customer.name == "MidRoller")
//          customer.getArrears.value shouldEqual 1250.0 +- 750.0
//        if (customer.name == "HighRoller")
//          customer.getArrears.value shouldEqual 2495.0 +- 2505.0
//      })
//    }
//  }
//
//  it should "return 200 OK testing data when given a valid set of training data to Train PUT route" in {
//
//    val configurations = mockData.getValidConfiguration()
//
//    val payload = mockData.getValidTrainingData()
//    val dbRet = Right(configurations)
//    val fakeStore = FakeStorage(dbRet)
//    val fakeDl = FakeDataLoader()
//    val fakeAnn = FakeArtificialNeuralNetwork()
//
//    Put("/train", payload) ~> callServiceConfigure(fakeStore, fakeDl, fakeAnn).train("TestUsername") ~> check {
//      status shouldEqual StatusCodes.OK
//      val data = entityAs[TrainingData]
//    }
//  }
//
//}
