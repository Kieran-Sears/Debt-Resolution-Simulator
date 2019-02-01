package actors

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.StatusCodes
import org.scalatest._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKitBase}
import akka.util.Timeout
import simulator.Settings
import simulator.actors.HttpService
import simulator.model.State

import scala.concurrent.{ExecutionContext, Future}

class HttpServiceSpec  extends FlatSpec
  with Matchers
  with Directives
  with ScalatestRouteTest
  with OptionValues
  with TestKitBase
  with ImplicitSender
  with BeforeAndAfterAll {

  def callService(
                   providerResponder: Option[BankDetailsReader] = None,
                   consumerResponder: Option[BankDetailsUpdater] = None
                 ): Route = {

    def service = new HttpService {
      override lazy val log: LoggingAdapter = Logging(system, classOf[HttpService])
      override implicit lazy val system: ActorSystem = outer.system
      // override implicit lazy val ec: ExecutionContext = system.dispatcher
      override implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
      override implicit lazy val timeout: Timeout = Timeout(5 seconds)

      override val stateActor: ActorRef = system.actorOf(Props(new Actor {
        def receive: PartialFunction[Any, Unit] = {
          case state: State => if (state.== engagement.id
          ) sender ! engagement
          case _: UpdateBankDetails => sender ! Done
        }
      }))

      service.route
    }
  }

  "POST play with a valid state" should "return a status code 404 " in {
    val result = Post(s"/play/") ~> callService(providerResponder = Option({
      case _ => Future.successful(Left(new HostUnsuccessfulResult {
        override def message: String = "failed"
      }))
    }), engagement = fakeEngagement)
    result ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }


  "GET Journey on existing account with a journey" should "return the account's journey" in {
    val fakeEngagement: Engagement = getEngagement
    val fakeAccount: Account = fakeEngagement.accounts.head
    val request = Get(s"/account/${fakeAccount.id}/journey/")
    val result = request ~> callService(engagement = fakeEngagement)
    result ~> check {
      entityAs[Journey] shouldEqual fakeAccount.journey.value
      mediaType shouldEqual `application/json`
      status shouldEqual StatusCodes.OK
    }
  }




}


  trait HostUnsuccessfulResult {

    def message: String

  }

  object HostUnsuccessfulResult {

    case class ValidationFailed(message: String) extends HostUnsuccessfulResult

    case class NotFound(message: String) extends HostUnsuccessfulResult

  }