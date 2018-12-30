package simulator.model.actions

import java.util.UUID

import simulator.model.State

case class Repeat(interval: Int, finishTime: Int)

trait SystemAction {
  val actionId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}

trait CustomerAction {
  val actionId: UUID
  val customerId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}

trait AgentAction {
  val actionId: UUID
  val repeat: Option[Repeat]
  val kind: String
  def perform(state: State): State

}