//package simulator.model.actions.customer
//
//import java.util.UUID
//import simulator.model._
//
//case class PayInFull(
//  id: UUID = UUID.randomUUID(),
//  actionId: UUID = UUID.randomUUID(),
//  name: String,
//  customerId: UUID,
//  repeat: Option[Repeat] = None,
//  kind: String = "payInFull")
//  extends CustomerAction {
//
//  override def perform(state: State) = {
//
//    val optCustomer = state.customers.find(customer => customer.id == customerId)
//    val customer = optCustomer.getOrElse(throw new Exception("Customer does not exist"))
//
//    val newTotalArrears = state.stats.totalArrears - customer.arrears
//
//    val newStats = state.stats.copy(totalArrears = newTotalArrears)
//
//    val customerListWithCustomerRemoved = state.removeCustomer(customerId)
//
//    val stateWithoutAction =
//      state.removeCustomerAction(state.time, actionId)
//
//    stateWithoutAction.copy(stats = newStats, customers = customerListWithCustomerRemoved)
//  }
//
//}
