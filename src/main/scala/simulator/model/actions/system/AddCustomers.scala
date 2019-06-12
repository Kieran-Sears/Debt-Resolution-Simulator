//package simulator.model.actions.system
//
//import java.util.UUID
//
//import simulator.Generator
//import simulator.model._
//
//case class AddCustomers(
//  id: UUID = UUID.randomUUID(),
//  name: String = "AddCustomers",
//  actionId: UUID = UUID.randomUUID(),
//  numberOfCustomers: Int,
//  arrearsBias: Double,
//  repeat: Option[Repeat],
//  kind: String = "addCustomers")
//  extends SystemAction {
//
//  override def perform(state: State): State = {
//    val stateWithoutAction =
//      state.removeSystemAction(state.time, actionId)
//    val newQueue = prepareNextRepetition(stateWithoutAction)
//    val customers = makeBatchOfCustomers(state)
//
//    val newStats = updateStatistics(state.stats, customers)
//    val updatedCustomerList = state.customers ++ customers
//    state.copy(stats = newStats, systemActions = newQueue, customers = updatedCustomerList)
//  }
//
//  def updateStatistics(stats: Statistics, newBatchOfCustomers: List[Customer]) = {
//    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)
//    val newTotalArrears = batchArrears + stats.totalArrears
//    Statistics(batchArrears, newTotalArrears)
//  }
//
//  def makeBatchOfCustomers(state: State): List[Customer] = {
//    state.configs.customerConfigurations
//      .flatMap(customerConf => {
//        val numberOfStereotype = state.configs.simulationConfiguration.numberOfCustomers / 100 * customerConf.proportion
//        (0 to numberOfStereotype).map(_ =>
//          Generator.default
//            .generateCustomer(customerConf, state.configs.attributeConfigurations, state.configs.optionConfigurations))
//      })
//  }
//
//  def calculateSumOfBatchArrears(batchOfCustomers: List[Customer]) = {
//    batchOfCustomers.foldLeft(0d)((acc, customer) => acc + customer.arrears)
//  }
//
//  def prepareNextRepetition(state: State) = {
//    repeat
//      .map(repeatInstructions => {
//        if (state.time < repeatInstructions.finishTime) {
//          state
//            .addSystemAction(
//              state.time + repeatInstructions.interval,
//              AddCustomers(
//                numberOfCustomers = numberOfCustomers,
//                arrearsBias = arrearsBias,
//                repeat = Some(repeatInstructions)))
//            .systemActions
//        } else {
//          state.systemActions
//        }
//      })
//      .getOrElse(state.systemActions)
//  }
//
//}
