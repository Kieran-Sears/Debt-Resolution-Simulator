package simple.DebtVsAge.model

import simple.DebtVsAge.model.Actions.Action

case class RunSimulation(config: SimulationConfig)

case class State(time: Int = 0,
                 stats: Statistics = Statistics(),
                 actionQueue: Map[Int, List[Action]] = Map(),
                 history: List[State] = Nil)

case class UpdateState(state: State)

case class StateUpdated(state: State)

case class TickOnTime(previousTime: Int, newTime: Int, stopTime: Option[Int])

case class TimeTickedOn(previousTime: Int, newTime: Int)

case class EndState(state: State)
