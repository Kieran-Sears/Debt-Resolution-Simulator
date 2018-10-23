package simple.DebtVsAge.model

case class State(time: Int = 0,
                 stats: Statistics = Statistics(),
                 actionQueue: ActionQueue = ActionQueue(),
                 history: List[State] = Nil)

case class UpdateState(state: State)

case class StateUpdated(state: State)

case class TickOnTime(previousTime: Int, newTime: Int)

case class TimeTickedOn(previousTime: Int, newTime: Int)

case class EndState(state: State)
