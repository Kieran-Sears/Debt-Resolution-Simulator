package simulator.actors

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.intel.analytics.bigdl.dlframes.{DLEstimator, DLModel}
import org.apache.spark.sql.DataFrame
import simulator.classifier.{ArtificialNeuralNetwork, SparkDataLoader}
import simulator.db.StorageController
import simulator.{Generator, Settings}
import simulator.model._

import scala.concurrent.Await

class HttpService(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val timeout: Timeout,
  implicit val storage: StorageController,
  implicit val ann: ArtificialNeuralNetwork,
  implicit val dl: SparkDataLoader
) extends MarshallingImplicits {

  lazy val log: LoggingAdapter = Logging(system, classOf[HttpService])
  lazy val gen: Generator = Generator.default

  val interfaceA = "localhost"
  val portA = 8080

  println(s"Starting Collaborate http interface at: $interfaceA:$portA")

  def myUserPassAuthenticator(credentials: Credentials): Option[String] =
    credentials match {
      case p @ Credentials.Provided(username) if p.verify(Settings().SecretSettings.sessionSecret) => Some(username)
      case _ => None
    }

  val route: Route = {
    authenticateBasic(realm = "Secure Site", myUserPassAuthenticator) { username =>
      options {
        complete(OK)
      } ~
      encodeResponse {
        configure(username) ~
        train(username) ~
        test ~
        play
      }
    }
  }

  def configure(username: String): Route = {
    pathPrefix("configure") {
      put {
        entity(as[Configurations]) { configs =>
          {
            val trainingData = gen.idealTrainingExamples(configs)
            storage.storeConfiguration(username, configs).unsafeRunSync()
            complete(OK, trainingData)
          }
        }
      }
    }
  }

  def train(username: String): Route = {
    pathPrefix("train") {
      put {
        entity(as[TrainingData]) { labelledData =>
          {
            storage.getConfiguration(labelledData.configurationId).unsafeRunSync() match {
              case Right(conf) => {

                val customers = gen.variedTrainingExamples(conf)

                val allOutcomes = Stats.getAllOutcomes(customers, labelledData)

                val idealMappings = Stats.getIdealMappings(allOutcomes)

                storage.storeTrainingData(labelledData).unsafeRunSync()

                val labels = dl.getLabelIndexMap(conf.actionConfigurations)

                storage
                  .storePlayingData(
                    username,
                    conf.attributeConfigurations,
                    idealMappings,
                    labelledData.configurationId,
                    labels)
                  .unsafeRunSync()

                val headers =
                  conf.attributeConfigurations.filter(_.attributeType == AttributeEnum.Global).map(_.name).toArray

                val inputs = headers.length
                val outputs = conf.actionConfigurations.length
                val hidden = Array(18, 36)

                println(s"inputs: $inputs, outputs: $outputs, hidden: ${hidden.toString}")

                val model = ann.createGraph(inputs, hidden, outputs)

                var workflow: (DLEstimator[Float], DataFrame, DataFrame, Int) => Unit = null

                workflow = (evaluator, train, test, _) => {
                  val modelDL: DLModel[Float] = ann.train(evaluator, train)
                  ann.test(modelDL, test)
                }

                val allHeaders = Array("id", "configuration_id", "customer_id") ++ headers.map(_.toLowerCase) ++ Array(
                  "action_label")

                val opts = Map(
                  "url" -> Settings().DatabaseSettings.simulatorUrl,
                  "dbtable" -> username,
                  "user" -> Settings().DatabaseSettings.user,
                  "password" -> Settings().SecretSettings.dbSecret
                )

                val Array(train, test) =
                  dl.assembleData(headers, dl.getData(opts, allHeaders)).randomSplit(Array(0.8, 0.2))

                val evaluator = ann.createEstimator(
                  model = model,
                  train = train,
                  epochs = 10,
                  learningRate = 0.001,
                  decayRate = 0.05,
                  batchSize = 92,
                  input = inputs,
                  output = outputs)

                workflow(evaluator, train, test, inputs)

                dl.stop

                complete(OK, TrainingData(conf.id, customers, labelledData.actions))
              }
              case Left(_) => complete(StatusCodes.BadRequest)
            }
          }
        }
      }
    }
  }

  def test: Route = {
    pathPrefix("test") {
      post {
        extractRequest map (request => {
          println("initialise Simulation endpoint request : " + request)
        })

        entity(as[State]) { startState =>
          {

            val stateActor =
              system.actorOf(Props(classOf[StateActor]), "stateActor_" + UUID.randomUUID())

            Await
              .result(stateActor ? RunSimulation(startState), timeout.duration)
              .asInstanceOf[SimulationComplete] match {
              case results: SimulationResults => complete(results)
              case error: SimulationError => complete(error.reason)
            }
          }
        }
      }
    }
  }

  def play: Route = {
    pathPrefix("play") {
      post {
        entity(as[State]) { state =>
          { // todo get list of actions player wants to do for this state
            complete("") // todo return current state
          }
        }
      }
    }
  }

  implicit def myRejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case MissingCookieRejection(cookieName) =>
          complete(HttpResponse(BadRequest, entity = "No cookies, no service!!!"))
      }
      .handle {
        case AuthorizationFailedRejection =>
          complete((Forbidden, "You're out of your depth!"))
      }
      .handle {
        case ValidationRejection(msg, _) =>
          complete((InternalServerError, "That wasn't valid! " + msg))
      }
      .handle {
        case e: Exception => {
          println("---------------- exception log start")
          println(e.getMessage, e)
          println("cause", e.getCause)
          println("cause", e.getStackTraceString)
          println(e)
          println("---------------- exception log end")
          Directives.complete("server made a boo boo")
        }
      }
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
      }
      .handleNotFound { complete((NotFound, "Not here!")) }
      .result()
}
