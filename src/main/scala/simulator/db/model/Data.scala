package simulator.db.model
import java.util.UUID

import simulator.model.{ActionEnum, AttributeEnum, VarianceEnum}

case class ActionData(id: UUID, name: String, repeat: Option[UUID], target: Option[UUID])
case class CustomerData(id: UUID, name: String, difficulty: Option[Int] = None, assignedLabel: Option[Int] = None)

case class CustomerConfigData(id: UUID, name: String, proportion: Int)
case class ActionConfigData(id: UUID, name: String, actionType: ActionEnum)
case class AttributeConfigData(id: UUID, name: String, value: UUID, attributeType: AttributeEnum)
case class CategoricalConfigData(id: UUID)
case class OptionConfigData(id: UUID, name: String, probability: Int)
case class ScalarConfigData(id: UUID, variance_type: VarianceEnum, min: Int, max: Int)
