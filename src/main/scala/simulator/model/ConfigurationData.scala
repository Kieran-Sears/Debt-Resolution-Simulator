package simulator.model
import java.util.UUID

case class Configurations(
  customerConfigurations: List[CustomerConfig] = Nil,
  actionConfigurations: List[ActionConfig] = Nil,
  effectConfigurations: List[EffectConfig] = Nil,
  attributeConfigurations: List[AttributeConfig] = Nil,
  scalarConfigurations: List[ScalarConfig] = Nil,
  categoricalConfigurations: List[CategoricalConfig] = Nil,
  optionConfigurations: List[OptionConfig] = Nil,
  repeatConfigurations: List[RepetitionConfig] = Nil,
  simulationConfiguration: SimulationConfig
)

case class SimulationConfig(
  id: UUID,
  startTime: Int = 0,
  endTime: Option[Int] = None,
  numberOfCustomers: Int = 10,
  kind: String = "simulation")

case class CustomerConfig(
  id: UUID,
  name: String,
  arrears: UUID,
  satisfaction: UUID,
  attributeConfigurations: List[UUID] = Nil,
  proportion: Int = 100,
  kind: String = "customer")

case class AttributeConfig(
  id: UUID,
  name: String,
  value: UUID
)

case class ScalarConfig(
  override val id: UUID,
  variance: Variance.Value,
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
  probability: Int,
  kind: String = "option"
)

case class ActionConfig(
  id: UUID,
  name: String,
  `type`: ActionType.Value,
  repeat: Option[UUID],
  effectConfigurations: List[UUID] = Nil,
  kind: String = "action"
)

case class EffectConfig(
  id: UUID,
  name: String,
  `type`: EffectType.Value,
  target: UUID,
  kind: String = "effect"
)

case class RepetitionConfig(
  id: UUID,
  interval: Int,
  repetitions: Int
)
