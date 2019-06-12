package simulator.model

import java.util.UUID

import jdk.jshell.spi.ExecutionControl.NotImplementedException

case class Repeat(interval: Int, finishTime: Int)

case class Action(id: UUID, name: String, effects: List[Effect], repeat: Option[Repeat], kind: String) {

  def perform(state: State): State = {
    ???
//    val optCustomer = state.customers.find(customer => customer.id == customerId)
//    val customer = optCustomer.getOrElse(throw new Exception("Customer does not exist"))
//
//    val newTotalArrears = state.stats.totalArrears - customer.arrears
//
//    val newStats = state.stats.copy(totalArrears = newTotalArrears)
//
//    val customerListWithCustomerRemoved = state.removeCustomer(customerId.getOrElse(throw NotImplementedException))
//
//    val stateWithoutAction =
//      state.removeAction(state.time, id)
//
//    stateWithoutAction.copy(stats = newStats, customers = customerListWithCustomerRemoved)
  }

}

case class Effect(
  id: UUID = UUID.randomUUID(),
  name: String,
  `type`: EffectType.Value,
  target: String,
  value: Option[Double] = None,
  certainty: Option[Int] = None,
  kind: String = "effect"
)

case class ActionConfig(
  id: UUID = UUID.randomUUID(),
  name: String,
  `type`: ActionType.Value,
  repeat: Option[Repeat],
  effectConfigurations: List[String] = Nil,
  kind: String = "action"
)

case class EffectConfig(
  id: UUID = UUID.randomUUID(),
  name: String,
  `type`: EffectType.Value,
  target: String,
  kind: String = "effect"
)
