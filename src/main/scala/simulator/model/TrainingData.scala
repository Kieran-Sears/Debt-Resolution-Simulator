package simulator.model
import java.util.UUID

import org.apache.commons.math3.distribution.NormalDistribution

import scala.util.Random

trait Train

case class Action(id: UUID, name: String, effects: List[Effect], /*repeat: Option[Repeat],*/ target: Option[Customer])
  extends Train {

  def perform(state: State): State = {
    val customer = getTarget

    val x = effects.map(effect => {
      val attributes = customer.attributes
      val attribute = attributes.filter(att => att.name == effect.target).head
      val actionValue = effect.deviation.get * Random.nextGaussian() + effect.value.get
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

    if (affectedEffects.isEmpty & affs.nonEmpty) {
      throw new NoSuchElementException(
        s"Affects should be directed " +
          s"toward effect names ($affectTargets) in $name")
    }

    val influences = affectedEffects.map(e => {
      val influence = affs.foldLeft(e.getValue)((acc, a) => {
        if (a.target == e.name) {
          val inf = a.calculateInfluence
          acc + inf
        } else {
          acc
        }
      })
      (e.target, influence)
    }) ++ effs.filter(p => !affectTargets.contains(p.name)).map(e => (e.target, e.calculateInfluence))

    val newAttributes = customer.attributes.map(att => {
      influences.find(x => x._1 == att.name) match {
        case Some((_, influ)) => att.copy(value = att.value + ((att.value / 100) * influ))
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
  deviation: Option[Int] = None
) extends Train {
  def calculateInfluence = {
    val mean =
      value.getOrElse(throw new NoSuchElementException(s"Could not find value for effect $name : $id for $target"))
    val standardDistribution =
      deviation.getOrElse(
        throw new NoSuchElementException(s"Could not find certainty for effect $name : $id for $target"))
    if (standardDistribution == 0) {
      mean
    } else {
      new NormalDistribution(mean, standardDistribution / 10).sample()
    }
  }

  def getValue = {
    value.getOrElse(
      throw new NoSuchElementException(s"Cannot get value for effect $name : $id, must not have been configured!"))
  }
}

case class Customer(
  id: UUID,
  name: String,
  attributes: List[Attribute] = Nil,
  difficulty: Option[Int] = None,
  assignedLabel: Option[Int] = None)
  extends Train {

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
) extends Train
