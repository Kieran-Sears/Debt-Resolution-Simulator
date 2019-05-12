package simulator.model

import java.util.UUID

case class Configurations(
  customerConfigurations: List[CustomerConfig] = Nil,
  actionConfigurations: List[ActionConfig] = Nil,
  simulationConfiguration: SimulationConfig = SimulationConfig()
)

case class CustomerConfig(
  id: String,
  attributeConfigurations: List[Attribute] = Nil,
  proportion: Int = 100,
  appearance: Variance.Value = Variance.None,
  assignedLabel: Int,
  kind: String = "customer")

case class ActionConfig(
  id: String
)

case class SimulationConfig(
  startState: Option[State] = None,
  startTime: Int = 0,
  endTime: Option[Int] = None,
  //debtVarianceOverTime: Variance.Value = Variance.None,
  numberOfCustomers: Int = 10,
  kind: String = "simulation")
