package simulator.actors

import java.util.NoSuchElementException

import akka.persistence.PersistentActor
import simulator.model._

import scala.util.{Failure, Success}

class StateActor extends PersistentActor {
  def persistenceId = "StateActor"
  var state: State = null
  var snapshotInterval = 10

  override def receiveCommand: Receive = {
    case run: RunSimulation => {
      val originalSender = sender()
      self ! UpdateState(run.state)
      self ! TickOnTime(run.state.time, 0, run.state.configs.simulationConfiguration.endTime, originalSender)
    }
    case tickOnTime: TickOnTime => {

      val stopEarly = tickOnTime.stopTime.contains(tickOnTime.newTime)
      val noActionsLeft = !state.hasSystemAction(tickOnTime.newTime.toString)
      val ranOutOfTime = tickOnTime.newTime == Int.MaxValue

      if (ranOutOfTime || noActionsLeft || stopEarly) {
        val results = generateSimulationresults(state)
        println("trying to return results back: " + results)
        tickOnTime.originalSender ! results
      }

      // perform actions on new time
      val stateWithUpdatedActionQueue = state.performActions(tickOnTime.newTime)

//      match {
//        case Success(stateWithUpdatedActionQueue) =>
      println(stateWithUpdatedActionQueue.stats)

      self ! UpdateState(stateWithUpdatedActionQueue.copy(time = tickOnTime.newTime, history = state.history :+ state))

      self ! TickOnTime(
        state.time,
        stateWithUpdatedActionQueue.getTimeOfNextSystemAction,
        tickOnTime.stopTime,
        tickOnTime.originalSender)
//        case Failure(error) =>
//          error match {
//            case _: NoSuchElementException =>
//              tickOnTime.originalSender ! SimulationError("Could not find next action for time " + tickOnTime.newTime)
//            case unknown: Throwable =>
//              tickOnTime.originalSender ! SimulationError(
//                "exception for performing actions not caught : " + unknown.getMessage)
//          }
//      }
    }

    case UpdateState(newState: State) => {
      if (newState.history.length % snapshotInterval == 0)
        saveSnapshot(state)
      state = newState

    }
  }

  val receiveRecover: Receive = {
    case StateUpdated(newState) =>
      this.state = newState
  }

  def generateSimulationresults(currentState: State) = {
    SimulationResults(
      batches = currentState.history
        .foldLeft[Map[String, Double]](Map())((acc, state: State) =>
          acc ++ Map(timeToString(state.time) -> state.stats.batchArrears)),
      totals = currentState.history
        .foldLeft[Map[String, Double]](Map())((acc, state: State) =>
          acc ++ Map(timeToString(state.time) -> state.stats.totalArrears)),
      aging = currentState.history
        .map(state => timeToString(state.time))
        .zip(
          (currentState.history :+ currentState).reverse.map(ts => ts.stats.batchArrears)
        )
        .toMap
    )
  }

  def timeToString(time: Int) = time.toString + "-" + (time + 10)

}
