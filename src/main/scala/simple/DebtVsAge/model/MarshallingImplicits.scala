package simple.DebtVsAge.model

import java.util.UUID

import simple.DebtVsAge.AkkaInterface.SimulationResults
import simple.DebtVsAge.model.Actions.{Action, AddCustomers, Repeat}
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
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
  implicit val customerGenParamsFormat = jsonFormat4(CustomerGenConfig)
  implicit val statisticsFormat = jsonFormat2(Statistics)
  implicit val repeatFormat = jsonFormat2(Repeat)
  implicit val addCustomersFormat = jsonFormat3(AddCustomers)

  implicit object ActionJsonFormat extends RootJsonFormat[Action] {
    def write(a: Action) = a match {
      case p: AddCustomers => p.toJson
    }

    def read(value: JsValue) =
      value.asJsObject.fields("kind") match {
        case JsString("addCustomers") => value.convertTo[AddCustomers]
        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
      }
  }

  implicit val statesFormat = jsonFormat4(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat4(State))
  implicit val simulationConfigFormat = jsonFormat4(SimulationConfig)
  implicit val simulationResultsFormat = jsonFormat3(SimulationResults)


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
