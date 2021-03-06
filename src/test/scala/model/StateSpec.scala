//package model
//
//import org.scalatest.{FlatSpec, Matchers, TryValues}
//import simulator.model.actions.system.AddCustomers
//import simulator.model.{State, SystemAction}
//
//class StateSpec extends FlatSpec with Matchers {
//  "State " should "insert action upon AddAction" in {
//    val addCustomers = AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    val expectedQueue = State(systemActions = Map("0" -> List(addCustomers)))
//    val state = State()
//    state.addAction(0, addCustomers) shouldEqual expectedQueue
//  }
//  it should "remove action upon removeAction" in {
//    val addCustomers1 = AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    val addCustomers2 = AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    State()
//      .addAction(0, addCustomers1)
//      .addAction(0, addCustomers2)
//      .removeAction(0, addCustomers2.actionId) shouldEqual State(systemActions = Map("0" -> List(addCustomers1)))
//  }
//  it should "remove action upon removeAction and remove key if no other actions are left" in {
//    val addCustomers = AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    State()
//      .addAction(0, addCustomers)
//      .removeAction(0, addCustomers.actionId) shouldEqual State()
//  }
//  it should "upon invoking performActions consume all " +
//    "actions in the action queue for a given time without affecting other times" in {
//    val addCustomers0: SystemAction =
//      AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    val addCustomers1: SystemAction =
//      AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    val addCustomers2: SystemAction =
//      AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//    val addCustomers3: SystemAction =
//      AddCustomers(numberOfCustomers = 1, arrearsBias = 10, repeat = None, kind = "addCustomers")
//
//    val initState = State(
//      time = 1,
//      systemActions = Map(
//        "0" -> List(addCustomers0),
//        "1" -> List(addCustomers1, addCustomers2),
//        "2" -> List(addCustomers3)
//      )
//    )
//
//    val expectedSystemActions = Map(
//      "0" -> List(addCustomers0),
//      "2" -> List(addCustomers3)
//    )
//
//    val newState = initState.performActions(1)
//    newState.systemActions shouldEqual expectedSystemActions
//  }
//}
