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

  // println(s"Starting Collaborate http interface at: $interfaceA:$portA")

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
                println(s"ConfigurationAfterStorage:\n$conf")

                val customers = gen.variedTrainingExamples(conf)

                println("customers:")
                customers foreach println

                println(s"labelledData:\n $labelledData")

                val allOutcomes = Stats.getAllOutcomes(customers, labelledData)
                println("allOutcomes:")
                allOutcomes foreach println

                val idealMappings = Stats.getIdealMappings(allOutcomes)

                println("idealMappings:")
                idealMappings foreach println

                storage.storeTrainingData(labelledData).unsafeRunSync()
                storage
                  .storePlayingData(conf.attributeConfigurations, idealMappings, labelledData.configurationId)
                  .unsafeRunSync()

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
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
      }
      .handleNotFound { complete((NotFound, "Not here!")) }
      .result()
}
