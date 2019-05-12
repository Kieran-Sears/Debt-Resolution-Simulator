package simulator.model

import java.util.UUID

import simulator.model.actions._

import scala.util.{Failure, Success, Try}

case class State(
  time: Int = 0,
  stats: Statistics = Statistics(),
  systemActions: Map[String, List[SystemAction]] = Map(),
  customerActions: Map[String, List[CustomerAction]] = Map(),
  agentActions: Map[String, List[AgentAction]] = Map(),
  customers: List[Customer] = Nil,
  history: List[State] = Nil,
  featureMap: IndexedSeq[Attribute] = IndexedSeq(),
  configs: Configurations = Configurations()) {

  /*
  cycles through all actions for the current time-frame updating the state
  as it goes, it also calculates the next time to tick the simulation onto
  based on the next chronological action in the updated states ActionQueue
   */
  def performActions(time: Int): Try[State] = {
    val currentActions = systemActions(time.toString)

    val stateWithUpdatedActions = currentActions.foldLeft(Try(this))((state, action) => {
      state match {
        case Success(s) => action.perform(s)
        case Failure(e) => Failure(e)
      }
    })
    stateWithUpdatedActions match {
      case Success(swua) => Success(swua.copy(history = history :+ this))
      case Failure(e) => Failure(e)
    }

  }

  def removeCustomer(customerId: UUID) = {
    customers.filterNot(customer => customer.id == customerId)
  }

  def addSystemAction(time: Int, action: SystemAction) =
    if (systemActions.keySet.contains(time.toString))
      this.copy(systemActions = systemActions + (time.toString -> (systemActions(time.toString) :+ action)))
    else this.copy(systemActions = systemActions + (time.toString -> List(action)))

  def removeSystemAction(time: Int, actionId: UUID) = {
    val entryWithActionRemoved = time.toString -> systemActions(time.toString)
      .filterNot(action => action.actionId == actionId)
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

  def addAgentAction(time: Int, action: AgentAction) =
    if (systemActions.keySet.contains(time.toString))
      this.copy(agentActions = agentActions + (time.toString -> (agentActions(time.toString) :+ action)))
    else this.copy(agentActions = agentActions + (time.toString -> List(action)))

  def removeAgentAction(time: Int, actionId: UUID) = {
    val entryWithActionRemoved = time.toString -> agentActions(time.toString)
      .filterNot(action => action.actionId == actionId)
    if (entryWithActionRemoved._2.isEmpty) {
      this.copy(agentActions = agentActions - time.toString)
    } else {
      this.copy(agentActions = agentActions + entryWithActionRemoved)
    }
  }

  def getTimeOfNextAgentAction: Int = Integer.parseInt(agentActions.keys.min)

  def hasAgentAction(time: String) = {
    agentActions.keySet.contains(time)
  }

  def agentActionsEmpty(): Boolean = {
    agentActions.isEmpty
  }

  def addCustomerAction(time: Int, action: CustomerAction) =
    if (customerActions.keySet.contains(time.toString))
      this.copy(customerActions = customerActions + (time.toString -> (customerActions(time.toString) :+ action)))
    else this.copy(customerActions = customerActions + (time.toString -> List(action)))

  def removeCustomerAction(time: Int, actionId: UUID) = {
    val entryWithActionRemoved = time.toString -> customerActions(time.toString)
      .filterNot(action => action.actionId == actionId)
    if (entryWithActionRemoved._2.isEmpty) {
      this.copy(customerActions = customerActions - time.toString)
    } else {
      this.copy(customerActions = customerActions + entryWithActionRemoved)
    }
  }

  def getTimeOfNextCustomerAction: Int = Integer.parseInt(customerActions.keys.min)

  def hasCustomerAction(time: String) = {
    customerActions.keySet.contains(time)
  }

  def customerActionsEmpty(): Boolean = {
    customerActions.isEmpty
  }
}
