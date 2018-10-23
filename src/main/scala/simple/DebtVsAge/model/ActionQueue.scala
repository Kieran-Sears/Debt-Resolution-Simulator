package simple.DebtVsAge.model

import java.util.UUID

import simple.DebtVsAge.model.Actions.Action

case class ActionQueue(queue: Map[Int, List[Action]] = Map()) {

  /*
  cycles through all actions for the current time-frame updating the state
  as it goes, it also calculates the next time to tick the simulation onto
  based on the next chronological action in the updated states ActionQueue
   */
  def performActions(time: Int, state: State) =
    queue(time).foldLeft((Int.MaxValue, state))((acc, action) => {
      val (actionExecutionTime, newState) = action.perform(acc._2)
      if (actionExecutionTime != -1 && actionExecutionTime < acc._1)
        (actionExecutionTime, newState)
      else
        (acc._1, newState)
    })

  def removeActionFromActionQueue(time: Int, actionId: UUID) =
    ActionQueue(queue + (time -> queue(time).filterNot(action =>
      action.actionId == actionId)))

  def addNewAction(time: Int, action: Action) = {
    val newQueue = queue + (time -> (queue(time) :+ action))
    ActionQueue(newQueue)
  }

  def getTimeOfNextAction(): Int = {
    queue.keys.min
  }
}
