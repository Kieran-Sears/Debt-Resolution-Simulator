package simulator.model.actions.system

import java.util.UUID

import simulator.Generator
import simulator.model.actions.{Repeat, SystemAction}
import simulator.model.{Customer, State, Statistics, Variance}
import cats.implicits._

import scala.util.{Failure, Success, Try}

case class AddCustomers(
  actionId: UUID = UUID.randomUUID(),
  numberOfCustomers: Int,
  arrearsBias: Double,
  repeat: Option[Repeat],
  kind: String = "addCustomers")
  extends SystemAction {

  override def perform(state: State): Try[State] = {
    val stateWithoutAction =
      state.removeSystemAction(state.time, actionId)
    val newQueue = prepareNextRepetition(stateWithoutAction)
    val newBatchOfCustomers = makeBatchOfCustomers(state)

    newBatchOfCustomers match {
      case Success(customers) => {
        val newStats = updateStatistics(state.stats, customers)
        val updatedCustomerList = state.customers ++ customers
        Success(state.copy(stats = newStats, systemActions = newQueue, customers = updatedCustomerList))
      }
      case Failure(e) => Failure(e)
    }

  }

  def updateStatistics(stats: Statistics, newBatchOfCustomers: List[Customer]) = {
    val batchArrears = calculateSumOfBatchArrears(newBatchOfCustomers)
    val newTotalArrears = batchArrears + stats.totalArrears
    Statistics(batchArrears, newTotalArrears)
  }

  def makeBatchOfCustomers(state: State): Try[List[Customer]] = {
    val currentTime = state.time

    val c: Try[List[Customer]] = state.configs.customerConfigurations
      .flatMap(customerConf => {
        val numberOfStereotype = state.configs.simulationConfiguration.numberOfCustomers / 100 * customerConf.proportion
        (0 to numberOfStereotype).map(_ => Generator.default.generateCustomer(customerConf, state.featureMap))
      })
      .sequence

//    c match {
//      case Success(customers) => {
//        Success(for (customer: Customer <- customers) yield {
//          state.configs.simulation.debtVarianceOverTime match {
//            case Variance.None =>
//              customer
//            case Variance.Increase =>
//              customer.copy(arrears = customer.arrears + (arrearsBias * currentTime))
//            case Variance.Decrease =>
//              customer.copy(arrears = customer.arrears - (arrearsBias * currentTime))
//            case _ => customer
//          }
//        })
//      }
//      case Failure(e) => Failure(e)
//    }
    c
  }

  def calculateSumOfBatchArrears(batchOfCustomers: List[Customer]) = {
    batchOfCustomers.foldLeft(0d)((acc, customer) => acc + customer.arrears)
  }

  def prepareNextRepetition(state: State) = {
    repeat
      .map(repeatInstructions => {
        if (state.time < repeatInstructions.finishTime) {
          state
            .addSystemAction(
              state.time + repeatInstructions.interval,
              AddCustomers(UUID.randomUUID(), numberOfCustomers, arrearsBias, Some(repeatInstructions)))
            .systemActions
        } else {
          state.systemActions
        }
      })
      .getOrElse(state.systemActions)
  }

}
