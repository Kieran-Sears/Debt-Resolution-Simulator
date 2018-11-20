package simulator.model

import java.util.UUID
import simulator.model.Actions.Action

case class ActionQueue(queue: Map[String, List[Action]])  {
 // private var queue: Map[String, List[Action]] = Map()

//  def apply(queue: Map[String, List[Action]] = Map()): Unit = {
//     ActionQueue(queue)
//  }

  /*
  cycles through all actions for the current time-frame updating the state
  as it goes, it also calculates the next time to tick the simulation onto
  based on the next chronological action in the updated states ActionQueue
   */
  def performActions(time: Int, state: State): (Int, State) =
    queue(time.toString)
      .foldLeft((Int.MaxValue, state))((acc, action) => {
        val (actionExecutionTime, newState) = action.perform(acc._2)
        if (actionExecutionTime != -1 && actionExecutionTime < acc._1)
          (actionExecutionTime, newState)
        else
          (acc._1, newState)
      })

  def removeAction(time: Int,
                   actionId: UUID): Unit =
    queue + (time.toString -> queue(time.toString).filterNot(action =>
      action.actionId == actionId))

  def addAction(time: Int, action: Action): Unit =
    if (queue.keySet.contains(time.toString))
      queue + (time.toString -> (queue(time.toString) :+ action))
    else queue + (time.toString -> List(action))

  def getTimeOfNextAction(queue: Map[Int, List[Action]]): Int = queue.keys.min

  def hasAction(time: String) = {
    queue.keySet.contains(time)
  }

  def isEmpty(): Boolean = {
    queue.isEmpty
  }
}
//def removeActionFromActionQueue(time: Int,
//actionId: UUID,
//queue: Map[Int, List[Action]]) = {
//  val newList = queue(time).filterNot(action => action.actionId == actionId)
//  if (newList.isEmpty) queue - time
//  else queue + (time -> newList)
//}
