package simple.DebtVsAge.model.Actions

import java.util.UUID

import simple.DebtVsAge.model.State

abstract class Action {
  val actionId: UUID
  val repeat: Option[Repeat]
  def perform(state: State): (Int, State)
}

case class Repeat(interval: Int, finishTime: Int)
