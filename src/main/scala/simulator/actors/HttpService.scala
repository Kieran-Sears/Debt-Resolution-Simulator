package simulator.actors

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import akka.util.Timeout
import simulator.classifier.{DataLoader, KerasModel}
import simulator.db.{StorageController, StorageImpl}
import simulator.{Generator, Settings}
import simulator.model._

import scala.concurrent.Await

class HttpService(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val timeout: Timeout,
  implicit val storage: StorageController
) extends MarshallingImplicits {

  lazy val log: LoggingAdapter = Logging(system, classOf[HttpService])
  lazy val gen: Generator = Generator.default

//  val dl = new DataLoader()
  val ann = new KerasModel()

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
            val trainingData = gen.trainingData(configs)
            println(s"CONFIGURE:\n $trainingData")
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
        entity(as[TrainingData]) { trainingData =>
          {
            storage.getConfiguration(trainingData.configurationId).unsafeRunSync() match {
              case Right(conf) => {
                val customers = gen.playData(conf)

                val allOutcomes: List[(Customer, List[(Action, Customer)])] = customers.map { c: Customer =>
                  (c, trainingData.actions.foldLeft(List[(Action, Customer)]()) {
                    case (acc, a) => if (a.getTarget.name == c.name) acc :+ (a, a.processCustomer(c)) else acc
                  })
                }

                val idealMappings: List[(Customer, Action)] = allOutcomes.map {
                  case (customer, processedList) => {
                    val bestActionCustomer = processedList.foldLeft(processedList.head) {
                      case ((x: Action, y: Customer), (a: Action, c: Customer)) => {
                        if (Stats.compareCustomers(y, c) == c) (a, c) else (x, y)
                      }
                    }
                    (customer, bestActionCustomer._1)
                  }
                }

                storage.storeTrainingData(trainingData).unsafeRunSync()
                storage
                  .storePlayingData(conf.attributeConfigurations, idealMappings, trainingData.configurationId)
                  .unsafeRunSync()

                complete(OK, TrainingData(conf.id, customers, trainingData.actions))

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
}
