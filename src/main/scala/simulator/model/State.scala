package simulator.model

import java.util.UUID

case class State(
  time: Int = 0,
  stats: Statistics = Statistics(),
  systemActions: Map[String, List[Action]] = Map(),
  customers: List[Customer] = Nil,
  history: List[State] = Nil,
  featureMap: IndexedSeq[AttributeConfig] = IndexedSeq(),
  configs: Configurations = Configurations()) {

  /*
  cycles through all actions for the current time-frame updating the state
  as it goes, it also calculates the next time to tick the simulation onto
  based on the next chronological action in the updated states ActionQueue
   */
  def performActions(time: Int): State = {
    val currentActions = systemActions(time.toString)

    val stateWithUpdatedActions = currentActions.foldLeft(this)((state, action) => {
      action.perform(state)
    })
    stateWithUpdatedActions.copy(history = history :+ this)
  }

  def removeCustomer(customerId: UUID) = {
    customers.filterNot(customer => customer.id == customerId)
  }

  def addAction(time: Int, action: Action) =
    if (systemActions.keySet.contains(time.toString))
      this.copy(systemActions = systemActions + (time.toString -> (systemActions(time.toString) :+ action)))
    else this.copy(systemActions = systemActions + (time.toString -> List(action)))

  def removeAction(time: Int, actionId: UUID) = {
    val entryWithActionRemoved = time.toString -> systemActions(time.toString)
      .filterNot(action => action.id == actionId)
    if (entryWithActionRemoved._2.isEmpty) {
      this.copy(systemActions = systemActions - time.toString)
    } else {
      this.copy(systemActions = systemActions + entryWithActionRemoved)
    }
  }

  def getTimeOfNextSystemAction: Int = Integer.parseInt(systemActions.keys.min)

  def hasSystemAction(time: String) = {
    systemActions.keySet.contains(time)
  }

  def systemActionsEmpty(): Boolean = {
    systemActions.isEmpty
  }

}

case class TrainingData(
  customers: List[Customer],
  actions: List[Action]
)
