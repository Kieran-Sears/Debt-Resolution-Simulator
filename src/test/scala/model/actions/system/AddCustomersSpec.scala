package model.actions.system

import org.scalatest.{FlatSpec, Matchers}
import simulator.model._
import simulator.model.actions.SystemAction
import simulator.model.actions.system.AddCustomers

class AddCustomersSpec extends FlatSpec with Matchers {

  "Add Customers Action when given an empty state " should
    "update the statistics of the state with the new Customers " in {
    val addCustomers: SystemAction = AddCustomers(numberOfCustomers = 1, startingDebt = 10, repeat = None, kind = "addCustomers")
    val initQueue = Map("0" -> List(addCustomers))
    val emptyStateWithActionInQueue = State(systemActions = initQueue)
    val newStateWithNoActions = addCustomers.perform(emptyStateWithActionInQueue)
    val expectedStats =  Statistics(10.0, 10.0)

    newStateWithNoActions.stats shouldEqual expectedStats
  }

  it should "update the customers list in the state" in {
    val addCustomers: SystemAction = AddCustomers(numberOfCustomers = 1, startingDebt = 10, repeat = None, kind = "addCustomers")
    val initQueue = Map("0" -> List(addCustomers))
    val emptyStateWithActionInQueue = State(systemActions = initQueue)
    val newStateWithNoActions = addCustomers.perform(emptyStateWithActionInQueue)
    val expectedState = State(stats = Statistics(10.0, 10.0))

    newStateWithNoActions.customers.length shouldEqual 1
  }

}
