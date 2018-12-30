package simulator.model

import java.util.UUID

import simulator.model.actions._
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import simulator.model.actions.customer.PayInFull
import simulator.model.actions.system.AddCustomers
import spray.json._

trait MarshallingImplicits extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object UUIDFormat extends JsonFormat[UUID] {
  def write(uuid: UUID) = JsString(uuid.toString)
  def read(value: JsValue) = {
    value match {
      case JsString(uuid) => UUID.fromString(uuid)
      case _              => throw new DeserializationException("Expected hexadecimal UUID string")
    }
  }
}

  implicit val enumConverter = enumFormat(DebtTimeVariance)
  implicit val customerGenParamsFormat = jsonFormat3(CustomerGenConfig)
  implicit val statisticsFormat = jsonFormat2(Statistics)
  implicit val repeatFormat = jsonFormat2(Repeat)
  implicit val customerFormat = jsonFormat2(Customer)

  // System Actions
  implicit val addCustomersFormat = jsonFormat5(AddCustomers)

  // Customer Actions
  implicit val payInFullFormat = jsonFormat4(PayInFull)

  // Agent Actions

  implicit val simulationConfigFormat = jsonFormat4(SimulationConfig)
  implicit val simulationResultsFormat = jsonFormat3(SimulationResults)
  implicit val simulationErrorFormat = jsonFormat1(SimulationError)
  implicit val configurationsFormat = jsonFormat2(Configurations)
  implicit val statesFormat = jsonFormat8(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat8(State))

  implicit object SystemActionJsonFormat extends RootJsonFormat[SystemAction] {
    def write(a: SystemAction) = a match {
      case p: AddCustomers => p.toJson
      case _ => throw DeserializationException("Not yet implemented marshalling for System Action")
    }

    def read(value: JsValue) =
      value.asJsObject.fields("kind") match {
        case JsString("addCustomers") => value.convertTo[AddCustomers]
        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
      }
  }
  implicit object CustomerActionJsonFormat extends RootJsonFormat[CustomerAction] {
    def write(a: CustomerAction) = a match {
      case p: PayInFull => p.toJson
      case _ => throw DeserializationException("Not yet implemented marshalling for Customer Action")
    }

    def read(value: JsValue) =
      value.asJsObject.fields("kind") match {
        case JsString("payInFull") => value.convertTo[PayInFull]
        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
      }
  }
  implicit object AgentActionJsonFormat extends RootJsonFormat[AgentAction] {
    def write(a: AgentAction) = a match {
      case _ => throw DeserializationException("Not yet implemented marshalling for Agent Action")
    }

    def read(value: JsValue) =
      value.asJsObject.fields("kind") match {
        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
      }
  }

  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

}
