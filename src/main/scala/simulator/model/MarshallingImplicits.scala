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
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case x: JsObject => {
          val id = x.fields("id").toString.replace(""""""", "")
          UUID.fromString(id)
        }
        case _ => throw DeserializationException(s"Expected hexadecimal UUID string")
      }
    }
  }

  implicit object EffectEnumJsonFormat extends RootJsonFormat[EffectEnum] {
    def write(a: EffectEnum) = {
      a match {
        case EffectEnum.Effect => EffectEnum.toEnum(EffectEnum.Effect).toJson
        case EffectEnum.Affect => EffectEnum.toEnum(EffectEnum.Affect).toJson
        case _ => throw DeserializationException(s"Not yet implemented marshalling for EffectEnum $a")
      }
    }
    def read(value: JsValue) = {
      EffectEnum
        .fromEnum(value.convertTo[String])
    }
  }

  implicit object VarianceEnumJsonFormat extends RootJsonFormat[VarianceEnum] {
    def write(a: VarianceEnum) = {
      a match {
        case VarianceEnum.Increase => VarianceEnum.toEnum(VarianceEnum.Increase).toJson
        case VarianceEnum.Decrease => VarianceEnum.toEnum(VarianceEnum.Decrease).toJson
        case VarianceEnum.None => VarianceEnum.toEnum(VarianceEnum.None).toJson
        case _ => throw DeserializationException(s"Not yet implemented marshalling for VarianceEnum $a")
      }
    }
    def read(value: JsValue) = {
      VarianceEnum
        .fromEnum(value.convertTo[String])
    }
  }

  implicit object ActionEnumJsonFormat extends RootJsonFormat[ActionEnum] {
    def write(a: ActionEnum) = {
      a match {
        case ActionEnum.Agent => ActionEnum.toEnum(ActionEnum.Agent).toJson
        case ActionEnum.Customer => ActionEnum.toEnum(ActionEnum.Customer).toJson
        case _ => throw DeserializationException(s"Not yet implemented marshalling for ActionEnum $a")
      }
    }
    def read(value: JsValue) = {
      ActionEnum
        .fromEnum(value.convertTo[String])
    }
  }

  implicit object AttributeEnumJsonFormat extends RootJsonFormat[AttributeEnum] {
    def write(a: AttributeEnum) = {
      a match {
        case AttributeEnum.Global => AttributeEnum.toEnum(AttributeEnum.Global).toJson
        case AttributeEnum.Override => AttributeEnum.toEnum(AttributeEnum.Override).toJson
        case _ => throw DeserializationException(s"Not yet implemented marshalling for AttributeEnum $a")
      }
    }
    def read(value: JsValue) = {
      AttributeEnum
        .fromEnum(value.convertTo[String])
    }
  }

  implicit val scalarConfigFormat: RootJsonFormat[ScalarConfig] = jsonFormat5(ScalarConfig)
  implicit val optionConfigFormat: RootJsonFormat[OptionConfig] = jsonFormat3(OptionConfig)
  implicit val categoricalConfigFormat: RootJsonFormat[CategoricalConfig] = jsonFormat3(CategoricalConfig)
  implicit val effectConfigFormat: RootJsonFormat[EffectConfig] = jsonFormat4(EffectConfig)
  implicit val FeatureValueFormat: RootJsonFormat[Attribute] = jsonFormat3(Attribute)
  implicit val attributeConfigFormat: RootJsonFormat[AttributeConfig] = jsonFormat4(AttributeConfig)
  implicit val customerConfigFormat: RootJsonFormat[CustomerConfig] = jsonFormat4(CustomerConfig)
  implicit val actionConfigFormat: RootJsonFormat[ActionConfig] = jsonFormat4(ActionConfig)
  implicit val statisticsFormat: RootJsonFormat[Statistics] = jsonFormat2(Statistics)
  implicit val customerFormat: RootJsonFormat[Customer] = jsonFormat5(Customer)
  implicit val simulationConfigFormat: RootJsonFormat[SimulationConfig] = jsonFormat4(SimulationConfig)
  implicit val simulationResultsFormat: RootJsonFormat[SimulationResults] = jsonFormat3(SimulationResults)
  implicit val simulationErrorFormat: RootJsonFormat[SimulationError] = jsonFormat1(SimulationError)
  implicit val configurationsFormat: RootJsonFormat[Configurations] = jsonFormat9(Configurations)
  implicit val effectFormat: RootJsonFormat[Effect] = jsonFormat6(Effect)
  implicit val actionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val trainingDataFormat: RootJsonFormat[TrainingData] = jsonFormat3(TrainingData)
  implicit val statesFormat: RootJsonFormat[State] = jsonFormat7(State)
  implicit val stateFormat: JsonFormat[State] = lazyFormat(jsonFormat7(State))

}
