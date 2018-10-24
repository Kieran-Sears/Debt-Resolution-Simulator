package simple.DebtVsAge.AkkaInterface

import akka.persistence.PersistentActor
import simple.DebtVsAge.{Main, View}
import simple.DebtVsAge.model._

class StateActor extends PersistentActor {
  def persistenceId = "StateActor"
  var state = State()

  override def receiveCommand: Receive = {
    case tickOnTime: TickOnTime => {

      if (tickOnTime.newTime == Int.MaxValue || !state.actionQueue.keySet
            .contains(tickOnTime.newTime)) {
        View.displaySimulationResults(state)
        System.exit(1)
      }

      // perform actions on new time
      val (nextChronologicalActionTime, stateWithUpdatedActionQueue) =
        ActionQueue.performActions(tickOnTime.newTime, state)

      println(stateWithUpdatedActionQueue.stats)

      self ! UpdateState(
        stateWithUpdatedActionQueue.copy(time = tickOnTime.newTime,
                                         history = state.history :+ state))

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
