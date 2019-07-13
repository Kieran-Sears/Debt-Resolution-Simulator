package simulator

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.intel.analytics.bigdl.utils.Engine
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import simulator.actors.HttpService
import simulator.classifier.{ArtificialNeuralNetwork, KerasModel, SparkDataLoader}
import simulator.db.{StorageController, StorageImpl}

import scala.concurrent.duration._

class Main(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val timeout: Timeout,
  implicit val store: StorageController,
  implicit val ann: ArtificialNeuralNetwork,
  implicit val dl: SparkDataLoader
) {
  val httpService: HttpService = new HttpService()

  val clientRouteLogged = DebuggingDirectives.logRequestResult("Client ReST", Logging.InfoLevel)(httpService.route)

  Http()
    .bindAndHandle(
      handler = clientRouteLogged,
      interface = httpService.interfaceA,
      port = httpService.portA,
      settings = ServerSettings(ConfigFactory.load)
    )
}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("Simulation")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val store = new StorageImpl()

  System.setProperty("hadoop.home.dir", "C:\\WinUtils\\")

  val conf =
    Engine.createSparkConf().setMaster("local[*]").setAppName("KerasNN")
  val spark: SparkSession = SparkSession.builder
    .config(conf)
    .getOrCreate()
  Engine.init

  implicit val dl = new SparkDataLoader(spark)
  implicit val ann = new KerasModel(spark)

  store.initialiseStorageTables().unsafeRunSync()
  store.initialiseTrainingTables().unsafeRunSync()

  val simulation = new Main()
}
