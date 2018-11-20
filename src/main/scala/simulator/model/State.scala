package simulator.model

case class State(time: Int = 0,
                 stats: Statistics = Statistics(),
                 actionQueue: ActionQueue = ActionQueue(),
                 history: List[State] = Nil)
