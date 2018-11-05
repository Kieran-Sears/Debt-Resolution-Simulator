package simple.DebtVsAge.model.Actions

import java.util.UUID

import simple.DebtVsAge.model.{Config, State}

case class Repeat(interval: Int, finishTime: Int)

abstract class Action {
  val actionId: UUID
  val repeat: Option[Repeat]
  val config: Config
  def perform(state: State): (Int, State)
}
