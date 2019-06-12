package simulator.model

import java.util.UUID

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import simulator.model.actions.customer.PayInFull
//import simulator.model.actions.system.AddCustomers
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

  implicit val effectTypeFormat = enumFormat(EffectType)
  implicit val actionTypeFormat = enumFormat(ActionType)
  implicit val timeVarianceFormat = enumFormat(Variance)
  implicit val scalarConfigFormat = jsonFormat4(Scalar)
  implicit val optionConfigFormat = jsonFormat4(OptionConfig)
  implicit val categoricalConfigFormat = jsonFormat2(Categorical)
  implicit val effectConfigFormat = jsonFormat5(EffectConfig)
  implicit val FeatureValueFormat = jsonFormat3(Attribute)

  implicit object AttributeValueJsonFormat extends RootJsonFormat[Value] {
    def write(a: Value) = {
      a match {
        case v: Scalar => v.toJson
        case v: Categorical => v.toJson
        case _ => throw DeserializationException("Not yet implemented marshalling for System Action")
      }
    }

    def read(value: JsValue) = {
      value.asJsObject.fields("kind") match {
        case JsString("Scalar") => value.convertTo[Scalar]
        case JsString("Categorical") => value.convertTo[Categorical]
        case _ => throw DeserializationException("Could not Unmarshal AttributeValue of Unknown Type")
      }
    }
  }

  implicit val repeatFormat = jsonFormat2(Repeat)
  implicit val attributeConfigFormat = jsonFormat3(AttributeConfig)
  implicit val customerConfigFormat = jsonFormat7(CustomerConfig)
  implicit val actionConfigFormat = jsonFormat6(ActionConfig)
  implicit val statisticsFormat = jsonFormat2(Statistics)
  implicit val customerFormat = jsonFormat7(Customer)

  // System Actions
  // implicit val addCustomersFormat = jsonFormat7(AddCustomers)

  // Customer Actions
  // implicit val payInFullFormat = jsonFormat6(PayInFull)

  // Agent Actions

  implicit val simulationConfigFormat = jsonFormat5(SimulationConfig)
  implicit val simulationResultsFormat = jsonFormat3(SimulationResults)
  implicit val simulationErrorFormat = jsonFormat1(SimulationError)
  implicit val configurationsFormat = jsonFormat6(Configurations)

//  implicit object ActionJsonFormat extends RootJsonFormat[Action] {
//    def write(a: Action) = a match {
//      case p: CustomerAction => p.toJson
//      case p: AgentAction => p.toJson
//      case p: SystemAction => p.toJson
//      case _ => throw DeserializationException("Not yet implemented marshalling for Action")
//    }
//
//    def read(value: JsValue) =
//      value.asJsObject.fields("kind") match {
//        case JsString("Customer") => value.convertTo[CustomerAction]
//        case JsString("Agent") => value.convertTo[AgentAction]
//        case _ => throw DeserializationException("Could not Unmarshal Action of Unknown Type")
//      }
//  }
//
//  implicit object SystemActionJsonFormat extends RootJsonFormat[SystemAction] {
//    def write(a: SystemAction) = a match {
//      case p: AddCustomers => p.toJson
//      case _ => throw DeserializationException("Not yet implemented marshalling for System Action")
//    }
//
//    def read(value: JsValue) =
//      value.asJsObject.fields("kind") match {
//        case JsString("AddCustomers") => value.convertTo[AddCustomers]
//        case _ => throw DeserializationException("Could not Unmarshal SystemAction of Unknown Type")
//      }
//  }
//
//  implicit object CustomerActionJsonFormat extends RootJsonFormat[CustomerAction] {
//    def write(a: CustomerAction) = a match {
//      case p: PayInFull => p.toJson
//      case _ => throw DeserializationException("Not yet implemented marshalling for Customer Action")
//    }
//
//    def read(value: JsValue) =
//      value.asJsObject.fields("kind") match {
//        case JsString("PayInFull") => value.convertTo[PayInFull]
//        case _ => throw DeserializationException("Could not Unmarshal CustomerAction of Unknown Type")
//      }
//  }
//  implicit object AgentActionJsonFormat extends RootJsonFormat[AgentAction] {
//    def write(a: AgentAction) = a match {
//      case _ => throw DeserializationException("Not yet implemented marshalling for Agent Action")
//    }
//
//    def read(value: JsValue) =
//      value.asJsObject.fields("kind") match {
//        case _ => throw DeserializationException("Could not Unmarshal AgentAction of Unknown Type")
//      }
//  }

  implicit val effectFormat = jsonFormat7(Effect)
  implicit val actionFormat = jsonFormat5(Action)
  implicit val trainingDataFormat = jsonFormat2(TrainingData)

  implicit val statesFormat = jsonFormat7(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat7(State))

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
