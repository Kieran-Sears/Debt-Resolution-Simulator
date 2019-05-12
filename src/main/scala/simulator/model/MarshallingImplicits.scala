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
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val timeVarianceFormat = enumFormat(Variance)
  implicit val scalarFormat = jsonFormat5(Scalar)
  implicit val categoricalFormat = jsonFormat3(Categorical)

  implicit object AttributeValueJsonFormat extends RootJsonFormat[Value] {
    def write(a: Value) = {
      println(a.toString)
      a match {
        case v: Scalar => v.toJson
        case v: Categorical => v.toJson
        case _ => throw DeserializationException("Not yet implemented marshalling for System Action")
      }
    }

    def read(value: JsValue) = {
      println(value.toString)
      value.asJsObject.fields("kind") match {
        case JsString("scalar") => value.convertTo[Scalar]
        case JsString("categorical") => value.convertTo[Categorical]
        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
      }
    }
  }

  implicit val attributeFormat = jsonFormat2(Attribute)
  implicit val customerGenParamsFormat = jsonFormat6(CustomerConfig)
  implicit val actionGenParamsFormat = jsonFormat1(ActionConfig)
  implicit val statisticsFormat = jsonFormat2(Statistics)
  implicit val repeatFormat = jsonFormat2(Repeat)
  implicit val customerFormat = jsonFormat5(Customer)

  // System Actions
  implicit val addCustomersFormat = jsonFormat5(AddCustomers)

  // Customer Actions
  implicit val payInFullFormat = jsonFormat4(PayInFull)

  // Agent Actions

  implicit val simulationConfigFormat = jsonFormat5(SimulationConfig)
  implicit val simulationResultsFormat = jsonFormat3(SimulationResults)
  implicit val simulationErrorFormat = jsonFormat1(SimulationError)
  implicit val configurationsFormat = jsonFormat3(Configurations)

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

  implicit val statesFormat = jsonFormat9(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat9(State))

  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse =>
            throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

}
