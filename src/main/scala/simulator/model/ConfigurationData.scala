package simulator.model
import java.util.UUID

case class Configurations(
  id: UUID,
  customerConfigurations: List[CustomerConfig] = Nil,
  actionConfigurations: List[ActionConfig] = Nil,
  effectConfigurations: List[EffectConfig] = Nil,
  attributeConfigurations: List[AttributeConfig] = Nil,
  scalarConfigurations: List[ScalarConfig] = Nil,
  categoricalConfigurations: List[CategoricalConfig] = Nil,
  optionConfigurations: List[OptionConfig] = Nil,
  // repeatConfigurations: List[RepetitionConfig] = Nil,
  simulationConfiguration: SimulationConfig
)

case class SimulationConfig(id: UUID, startTime: Int = 0, endTime: Option[Int] = None, numberOfCustomers: Int = 10)

case class CustomerConfig(id: UUID, name: String, attributeOverrides: List[UUID] = Nil, proportion: Int = 100)

case class AttributeConfig(
  id: UUID,
  name: String,
  value: UUID,
  attributeType: AttributeEnum
)

trait Value {
  val id: UUID
  val kind: String
}

case class ScalarConfig(
  override val id: UUID,
  variance: VarianceEnum,
  min: Double,
  max: Double,
  override val kind: String = "scalar"
) extends Value

case class CategoricalConfig(
  override val id: UUID,
  options: List[UUID],
  override val kind: String = "categorical"
) extends Value

case class OptionConfig(
  id: UUID,
  name: String,
  probability: Int
)

case class ActionConfig(
  id: UUID,
  name: String,
  actionType: ActionEnum,
  // repeat: Option[UUID],
  effectConfigurations: List[UUID]
)

case class EffectConfig(
  id: UUID,
  name: String,
  effectType: EffectEnum,
  target: String
)

//case class RepetitionConfig(
//  id: UUID,
//  interval: Int,
//  repetitions: Int
//)
