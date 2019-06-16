package simulator.model
import java.util.UUID

case class Action(id: UUID, name: String, effects: List[Effect], repeat: Option[UUID], kind: String) {

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
  id: UUID,
  name: String,
  `type`: EffectType.Value,
  target: UUID,
  value: Option[Double] = None,
  certainty: Option[Int] = None,
  kind: String = "effect"
)

case class Customer(
  id: UUID,
  name: String,
  arrears: Double,
  satisfaction: Double,
  featureValues: List[UUID] = Nil,
  difficulty: Option[Int] = None,
  assignedLabel: Option[Int] = None)

case class Attribute(
  id: UUID,
  name: String,
  value: Double
)

trait Value {
  val id: UUID
  val kind: String
}
