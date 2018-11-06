package simulator.actors

import akka.persistence.PersistentActor
import simulator.model._

class StateActor extends PersistentActor {
  def persistenceId = "StateActor"
  var state = State()
  var snapshotInterval = 10

  override def receiveCommand: Receive = {
    case tickOnTime: TickOnTime => {

      val stopEarly = tickOnTime.stopTime.contains(tickOnTime.newTime)
      val noActionsLeft = !state.actionQueue.keySet.contains(tickOnTime.newTime)
      val ranOutOfTime = tickOnTime.newTime == Int.MaxValue

      if (ranOutOfTime || noActionsLeft || stopEarly) {
        // TODO return state to original sender
      }

      // perform actions on new time
      val (nextChronologicalActionTime, stateWithUpdatedActionQueue) =
        ActionQueue.performActions(tickOnTime.newTime, state)

      println(stateWithUpdatedActionQueue.stats)

      self ! UpdateState(
        stateWithUpdatedActionQueue.copy(time = tickOnTime.newTime,
                                         history = state.history :+ state))

      self ! TickOnTime(state.time, nextChronologicalActionTime, tickOnTime.stopTime)

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

}
