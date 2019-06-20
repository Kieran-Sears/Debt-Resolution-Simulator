package simulator.model
import spray.json.DeserializationException

sealed trait VarianceEnum
object VarianceEnum {
  case object Increase extends VarianceEnum
  case object Decrease extends VarianceEnum
  case object None extends VarianceEnum
  def toEnum(e: VarianceEnum): String =
    e match {
      case Increase => "Increase"
      case Decrease => "Decrease"
      case None => "Decrease"
    }
  def fromEnum(s: String): VarianceEnum =
    (Option(s) collect {
      case "Increase" => Increase
      case "Decrease" => Decrease
      case "None" => None
    }).getOrElse(throw DeserializationException(s"Could not Unmarshal VarianceEnum $s"))
}

sealed trait EffectEnum
object EffectEnum {
  case object Effect extends EffectEnum
  case object Affect extends EffectEnum

  def toEnum(e: EffectEnum): String =
    e match {
      case Effect => "Effect"
      case Affect => "Affect"
    }
  def fromEnum(s: String): EffectEnum =
    (Option(s) collect {
      case "Effect" => Effect
      case "Affect" => Affect
    }).getOrElse(throw DeserializationException(s"Could not Unmarshal EffectEnum $s"))
}

sealed trait ActionEnum
object ActionEnum {
  case object Customer extends ActionEnum
  case object Agent extends ActionEnum

  def toEnum(e: ActionEnum): String =
    e match {
      case Customer => "Customer"
      case Agent => "Agent"
    }
  def fromEnum(s: String): ActionEnum =
    (Option(s) collect {
      case "Customer" => Customer
      case "Agent" => Agent
    }).getOrElse(throw DeserializationException(s"Could not Unmarshal ActionEnum $s"))
}

sealed trait AttributeEnum
object AttributeEnum {
  case object Global extends AttributeEnum
  case object Override extends AttributeEnum

  def toEnum(e: AttributeEnum): String =
    e match {
      case Global => "Global"
      case Override => "Override"
    }
  def fromEnum(s: String): AttributeEnum =
    (Option(s) collect {
      case "Global" => Global
      case "Override" => Override
    }).getOrElse(throw DeserializationException(s"Could not Unmarshal AttributeEnum $s"))
}
