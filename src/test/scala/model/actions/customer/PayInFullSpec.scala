package model.actions.customer

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import simulator.model._
import simulator.model.actions.{CustomerAction}
import simulator.model.actions.customer.PayInFull

class PayInFullSpec extends FlatSpec with Matchers {

  "Pay in Full Action when given an empty state " should
    "update the statistics of the state with the new Customers " in {
    val customerId = UUID.randomUUID()
    val customer = Customer(customerId, arrears = 200)
    val payInFull: CustomerAction = PayInFull(customerId = customerId, repeat = None, kind = "payInFull")
    val initQueue = Map("0" -> List(payInFull))
    val emptyStateWithActionInQueue = State(customerActions = initQueue, customers = List(customer))
    val newStateWithNoActions = payInFull.perform(emptyStateWithActionInQueue)
    val expectedStats =  Statistics(0, -200.0)

    emptyStateWithActionInQueue.stats shouldEqual Statistics()
    newStateWithNoActions.stats shouldEqual expectedStats
  }
  it should "remove the customer from the states customer list" in {
    val customerId = UUID.randomUUID()
    val customer = Customer(customerId, arrears = 200)
    val payInFull: CustomerAction = PayInFull(customerId = customerId, repeat = None, kind = "payInFull")
    val initQueue = Map("0" -> List(payInFull))
    val emptyStateWithActionInQueue = State(customerActions = initQueue, customers = List(customer))
    val newStateWithNoActions = payInFull.perform(emptyStateWithActionInQueue)

    emptyStateWithActionInQueue.customers shouldEqual List(customer)
    newStateWithNoActions.customers shouldEqual Nil
  }
}