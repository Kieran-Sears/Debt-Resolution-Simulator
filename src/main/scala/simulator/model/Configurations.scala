package simulator.model

case class Configurations(
  customerConfigurations: List[CustomerConfig] = Nil,
  actionConfigurations: List[ActionConfig] = Nil,
  effectConfigurations: List[EffectConfig] = Nil,
  attributeConfigurations: List[AttributeConfig] = Nil,
  optionConfigurations: List[OptionConfig] = Nil,
  simulationConfiguration: SimulationConfig = SimulationConfig()
)

case class SimulationConfig(
  startState: Option[State] = None,
  startTime: Int = 0,
  endTime: Option[Int] = None,
  //debtVarianceOverTime: Variance.Value = Variance.None,
  numberOfCustomers: Int = 10,
  kind: String = "simulation")
