package simulator.model

import java.util.UUID

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait MarshallingImplicits extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object OptionUUIDFormat extends JsonFormat[Option[UUID]] {
    def write(uuid: Option[UUID]) = {
      uuid match {
        case Some(id) => JsString(id.toString)
        case None => JsNull
      }
    }
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => Some(UUID.fromString(uuid))
        case x: JsObject => {
          val id = x.fields("id").toString.replace(""""""", "")
          Some(UUID.fromString(id))
        }
        case JsNull => None
        case _ => throw DeserializationException(s"Expected Optional hexadecimal UUID string")
      }
    }
  }

  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = {
      JsString(uuid.toString)
    }
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case x: JsObject => {
          val id = x.fields("id").toString.replace(""""""", "")
          println(id)
          UUID.fromString(id)
        }
        case _ => throw DeserializationException(s"Expected hexadecimal UUID string")
      }
    }
  }

  implicit val effectTypeFormat: RootJsonFormat[EffectType.Value] = enumFormat(EffectType)
  implicit val actionTypeFormat: RootJsonFormat[ActionType.Value] = enumFormat(ActionType)
  implicit val timeVarianceFormat: RootJsonFormat[Variance.Value] = enumFormat(Variance)
  implicit val scalarConfigFormat: RootJsonFormat[ScalarConfig] = jsonFormat5(ScalarConfig)
  implicit val optionConfigFormat: RootJsonFormat[OptionConfig] = jsonFormat4(OptionConfig)
  implicit val categoricalConfigFormat: RootJsonFormat[CategoricalConfig] = jsonFormat3(CategoricalConfig)
  implicit val effectConfigFormat: RootJsonFormat[EffectConfig] = jsonFormat5(EffectConfig)
  implicit val FeatureValueFormat: RootJsonFormat[Attribute] = jsonFormat3(Attribute)
  implicit val repeatConfigFormat: RootJsonFormat[RepetitionConfig] = jsonFormat3(RepetitionConfig)
  implicit val repeatFormat: RootJsonFormat[Repeat] = jsonFormat4(Repeat)
  implicit val attributeConfigFormat: RootJsonFormat[AttributeConfig] = jsonFormat3(AttributeConfig)
  implicit val customerConfigFormat: RootJsonFormat[CustomerConfig] = jsonFormat5(CustomerConfig)
  implicit val actionConfigFormat: RootJsonFormat[ActionConfig] = jsonFormat5(ActionConfig)
  implicit val statisticsFormat: RootJsonFormat[Statistics] = jsonFormat2(Statistics)
  implicit val customerFormat: RootJsonFormat[Customer] = jsonFormat5(Customer)
  implicit val simulationConfigFormat: RootJsonFormat[SimulationConfig] = jsonFormat5(SimulationConfig)
  implicit val simulationResultsFormat: RootJsonFormat[SimulationResults] = jsonFormat3(SimulationResults)
  implicit val simulationErrorFormat: RootJsonFormat[SimulationError] = jsonFormat1(SimulationError)
  implicit val configurationsFormat: RootJsonFormat[Configurations] = jsonFormat11(Configurations)
  implicit val effectFormat: RootJsonFormat[Effect] = jsonFormat6(Effect)
  implicit val actionFormat: RootJsonFormat[Action] = jsonFormat5(Action)
  implicit val trainingDataFormat: RootJsonFormat[TrainingData] = jsonFormat2(TrainingData)
  implicit val statesFormat: RootJsonFormat[State] = jsonFormat7(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat7(State))

  implicit object ValueJsonFormat extends RootJsonFormat[Value] {

    def write(a: Value) = {
      a match {
        case v: ScalarConfig => v.toJson
        case v: CategoricalConfig => v.toJson
        case _ => throw DeserializationException("Not yet implemented marshalling for System Action")
      }
    }

    def read(value: JsValue) = {
      val id = value.asJsObject.fields("id")
      value.asJsObject.fields("kind") match {
        case JsString("scalar") => value.convertTo[ScalarConfig].copy(id = id.convertTo[UUID])
        case JsString("categorical") => value.convertTo[CategoricalConfig].copy(id = id.convertTo[UUID])
        case _ => throw DeserializationException("Could not Unmarshal AttributeValue of Unknown Type")
      }
    }
  }

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
