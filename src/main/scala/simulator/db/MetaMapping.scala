package simulator.db

import java.util.UUID

import doobie.postgres.implicits.pgEnum
import doobie.util.{Get, Put}
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.model._

trait MetaMapping {
  implicit val VarianceMeta = pgEnum(Variance, "Variance")
  implicit val ActionTypeMeta = pgEnum(ActionType, "type")
  implicit val EffectTypeMeta = pgEnum(EffectType, "type")

//  implicit val customerGet: Get[Customer] =
//    Get[(UUID, String, List[Attribute], Option[Int], Option[String])].map(getCustomer)
//  implicit val effectPut: Put[Customer] =
//    Put[(UUID, String, List[Attribute], Option[Int], Option[String])].contramap(putCustomer)
//
//  def getCustomer(
//    id: UUID,
//    name: String,
//    featureValues: List[Attribute],
//    difficulty: Option[Int],
//    assignedLabel: Option[Int]) = {
//    Customer(id, name, featureValues, difficulty, assignedLabel)
//  }
//
//  def putCustomer(
//    id: UUID,
//    name: String,
//    featureValues: List[Attribute],
//    difficulty: Option[Int],
//    assignedLabel: Option[Int]) = {
//    (id, String, Option[Int], Option[String])
//  }
//
//  implicit val attGet: Get[List[Attribute]] = Get[List[String]].map(getAttribute)
//  implicit val attPut: Put[List[Attribute]] = Put[List[String]].contramap(putAttribute)
//
//  def getAttribute(ids: List[String]) = {
//    ids.map(id => StorageImpl.attributeStorage.readById(UUID.fromString(id)).unsafeRunSync().head)
//  }
//
//  def putAttribute(attributes: List[Attribute]) = {
//    attributes.map(_.id.toString)
//  }
}
