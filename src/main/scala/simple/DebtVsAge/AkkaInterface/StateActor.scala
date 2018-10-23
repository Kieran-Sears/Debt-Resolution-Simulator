package simple.DebtVsAge.AkkaInterface

import akka.persistence.PersistentActor
import simple.DebtVsAge.{Main, View}
import simple.DebtVsAge.model._

class StateActor extends PersistentActor {
  def persistenceId = "StateActor"
  var state = State()

  override def receiveCommand: Receive = {
    case tickOnTime: TickOnTime => {

      if (tickOnTime.newTime == Int.MaxValue) {}

      // perform actions on new time
      val (nextChronologicalActionTime, stateWithUpdatedActionQueue) =
        state.actionQueue.performActions(tickOnTime.newTime, state)

      self ! UpdateState(
        stateWithUpdatedActionQueue.copy(time = tickOnTime.newTime))

      self ! TickOnTime(state.time, nextChronologicalActionTime)

    }

    case UpdateState(newState: State) => {

      if (newState.history.length % Main.snapshotInterval == 0)
        saveSnapshot(state)
      state = newState

    }

    case EndState(state: State) => {
      View.displaySimulationResults(state)
    }
  }

  val receiveRecover: Receive = {
    case StateUpdated(newState) =>
      this.state = newState
  }

}
