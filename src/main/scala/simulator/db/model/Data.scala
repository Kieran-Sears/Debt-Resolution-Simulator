package simulator.db.model
import java.util.UUID

import simulator.model.{ActionEnum, AttributeEnum, VarianceEnum}

case class ActionData(id: UUID, name: String, repeat: Option[UUID], target: Option[UUID])
case class CustomerData(id: UUID, name: String, difficulty: Option[Int] = None, assignedLabel: Option[Int] = None)

trait ConfigDbData

case class CustomerConfigData(id: UUID, name: String, proportion: Int) extends ConfigDbData
case class ActionConfigData(id: UUID, name: String, actionType: ActionEnum) extends ConfigDbData
case class AttributeConfigData(id: UUID, name: String, value: UUID, attributeType: AttributeEnum) extends ConfigDbData
case class CategoricalConfigData(id: UUID, configurationId: UUID, attributeId: UUID) extends ConfigDbData
case class OptionConfigData(id: UUID, name: String, probability: Int) extends ConfigDbData
case class ScalarConfigData(id: UUID, variance_type: VarianceEnum, min: Int, max: Int) extends ConfigDbData
