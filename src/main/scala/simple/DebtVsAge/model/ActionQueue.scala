package simple.DebtVsAge.model

import java.util.UUID

import simple.DebtVsAge.model.Actions.Action

object ActionQueue {
  // private var queue: Map[Int, List[Action]] = Map()
  /*
  cycles through all actions for the current time-frame updating the state
  as it goes, it also calculates the next time to tick the simulation onto
  based on the next chronological action in the updated states ActionQueue
   */
  def performActions(time: Int, state: State) =
    state
      .actionQueue(time)
      .foldLeft((Int.MaxValue, state))((acc, action) => {
        val (actionExecutionTime, newState) = action.perform(acc._2)
        if (actionExecutionTime != -1 && actionExecutionTime < acc._1)
          (actionExecutionTime, newState)
        else
          (acc._1, newState)
      })

  def removeActionFromActionQueue(time: Int,
                                  actionId: UUID,
                                  queue: Map[Int, List[Action]]) =
    queue + (time -> queue(time).filterNot(action =>
      action.actionId == actionId))

  def addNewAction(time: Int, action: Action, queue: Map[Int, List[Action]]) =
    if (queue.keySet.contains(time)) queue + (time -> (queue(time) :+ action))
    else queue + (time -> List(action))

  def getTimeOfNextAction(queue: Map[Int, List[Action]]): Int = queue.keys.min

}
//def removeActionFromActionQueue(time: Int,
//actionId: UUID,
//queue: Map[Int, List[Action]]) = {
//  val newList = queue(time).filterNot(action => action.actionId == actionId)
//  if (newList.isEmpty) queue - time
//  else queue + (time -> newList)
//}
