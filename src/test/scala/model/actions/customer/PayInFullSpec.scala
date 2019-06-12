//package model.actions.customer
//
//import org.scalatest.{FlatSpec, Matchers, TryValues}
//import simulator.model._
//import simulator.model.actions.customer.PayInFull
//
//class PayInFullSpec extends FlatSpec with Matchers with TryValues {
//
//  "Pay in Full Action when given an empty state " should
//    "update the statistics of the state with the new Customers " in {
//    val customerName = "Mary"
//    val customer = Customer(name = customerName, arrears = 200, satisfaction = 50, assignedLabel = Some(0))
//    val payInFull: CustomerAction =
//      PayInFull(name = "PayInFull", customerId = customer.id, repeat = None, kind = "payInFull")
//    val initQueue = Map("0" -> List(payInFull))
//    val emptyStateWithActionInQueue = State(customerActions = initQueue, customers = List(customer))
//    val newStateWithNoActions = payInFull.perform(emptyStateWithActionInQueue)
//    val expectedStats = Statistics(0, -200.0)
//
//    emptyStateWithActionInQueue.stats shouldEqual Statistics()
//    newStateWithNoActions.stats shouldEqual expectedStats
//  }
//  it should "remove the customer from the states customer list" in {
//    val customer = Customer(name = "Phil", arrears = 200, satisfaction = 50, assignedLabel = Some(0))
//    val payInFull: CustomerAction =
//      PayInFull(name = "PayInFull", customerId = customer.id, repeat = None, kind = "payInFull")
//    val initQueue = Map("0" -> List(payInFull))
//    val emptyStateWithActionInQueue = State(customerActions = initQueue, customers = List(customer))
//    val newStateWithNoActions = payInFull.perform(emptyStateWithActionInQueue)
//
//    emptyStateWithActionInQueue.customers shouldEqual List(customer)
//    newStateWithNoActions.customers shouldEqual Nil
//  }
//}
