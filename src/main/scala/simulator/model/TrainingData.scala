package simulator.model
import java.util.UUID

import org.apache.commons.math3.distribution.NormalDistribution

import scala.util.Random

case class Action(id: UUID, name: String, effects: List[Effect], /*repeat: Option[Repeat],*/ target: Option[Customer]) {

  def perform(state: State): State = {
    val customer = getTarget

    val x = effects.map(effect => {
      val attributes = customer.attributes
      val attribute = attributes.filter(att => att.name == effect.target).head
      val actionValue = effect.certainty.get * Random.nextGaussian() + effect.value.get
      val important = (customer.name, attribute.name, attribute.value, actionValue)
      important // todo
    })

    val newTotalArrears = state.stats.totalArrears - customer.getArrears.value

    val newStats = state.stats.copy(totalArrears = newTotalArrears)

    val customerListWithCustomerRemoved = state.removeCustomer(customer.id)

    val stateWithoutAction = state.removeAction(state.time, id)

    stateWithoutAction.copy(stats = newStats, customers = customerListWithCustomerRemoved)
  }

  def processCustomer(customer: Customer): Customer = {
    val affs = effects.filter(_.effectType == EffectEnum.Affect)
    val effs = effects.filter(_.effectType == EffectEnum.Effect)

    val affectTargets = affs.map(_.target)

    val affectedEffects = effs.filter(p => affectTargets.contains(p.name))

    val influences = affectedEffects.map(e => {

      val influence = affs.foldLeft(e.getValue)((acc, a) => {
        if (a.target == e.name) {
          acc + a.calculateInfluence
        } else {
          acc
        }
      })
      (e.target, influence)
    })

    val newAttributes = customer.attributes.map(att => {
      influences.find(x => x._1 == att.name) match {
        case Some((_, influ)) => att.copy(value = att.value + influ)
        case None => att
      }
    })
    customer.copy(attributes = newAttributes)
  }

  def getTarget =
    target.getOrElse(throw new NoSuchElementException(s"target for action $name could not be found"))

}

case class Effect(
  id: UUID,
  name: String,
  effectType: EffectEnum,
  target: String,
  value: Option[Double] = None,
  certainty: Option[Int] = None
) {
  def calculateInfluence = {
    val mean =
      value.getOrElse(throw new NoSuchElementException(s"Could not find value for effect $name : $id for $target"))
    val standardDistribution =
      certainty.getOrElse(
        throw new NoSuchElementException(s"Could not find certainty for effect $name : $id for $target"))
    val dist = new NormalDistribution(mean, standardDistribution)
    dist.sample()
  }

  def getValue = {
    value.getOrElse(throw new Exception(s"Cannot get value for effect $name : $id, must not have been configured!"))
  }
}

case class Customer(
  id: UUID,
  name: String,
  attributes: List[Attribute] = Nil,
  difficulty: Option[Int] = None,
  assignedLabel: Option[Int] = None) {

  def getArrears = {
    attributes
      .find(_.name == "Arrears")
      .getOrElse(throw new NoSuchElementException(s"Cannot find Arrears for customer $name"))
  }

  def getSatisfaction = {
    attributes
      .find(_.name == "Satisfaction")
      .getOrElse(throw new NoSuchElementException(s"Cannot find Satisfaction for customer $name"))
  }
}

case class Attribute(
  id: UUID,
  name: String,
  value: Double
)

//case class Repeat(
//  id: UUID,
//  total: Int,
//  next: Int,
//  left: Int
//)
