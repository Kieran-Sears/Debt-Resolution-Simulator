package simulator.db

import doobie.util.Meta
import simulator.model._
import java.util.UUID
import doobie._
import doobie.implicits._

import spray.json._
import org.postgresql.util.PGobject
import simulator.db.model.{ActionData, CustomerData}

// postgres imports necessary for Option and UUID:
import doobie.postgres._
import doobie.postgres.implicits._

trait MetaMapping extends MarshallingImplicits {
  implicit val VarianceEnumMeta: Meta[VarianceEnum] = Meta[String].imap(VarianceEnum.fromEnum)(VarianceEnum.toEnum)
  implicit val ActionEnumMeta: Meta[ActionEnum] = Meta[String].imap(ActionEnum.fromEnum)(ActionEnum.toEnum)
  implicit val EffectEnumMeta: Meta[EffectEnum] = Meta[String].imap(EffectEnum.fromEnum)(EffectEnum.toEnum)
  implicit val AttributeEnumMeta: Meta[AttributeEnum] = Meta[String].imap(AttributeEnum.fromEnum)(AttributeEnum.toEnum)

//  implicit val customerRead: Read[CustomerData] =
//    Read[(UUID, String, List[UUID], Option[Int], Option[Int])].map {
//      case (a, b, c, d, e) => CustomerData(a, b, c, d, e)
//    }
//
//  implicit val customerWrite: Write[CustomerData] =
//    Write[(UUID, String, List[UUID], Option[Int], Option[Int])].contramap(p =>
//      (p.id, p.name, p.attributes, p.difficulty, p.assignedLabel))
//
//  implicit val actionRead: Read[ActionData] =
//    Read[(UUID, String, List[UUID], Option[UUID], Option[UUID])].map {
//      case (a, b, c, d, e) => ActionData(a, b, c, d, e)
//    }
//
//  implicit val actionWrite: Write[ActionData] =
//    Write[(UUID, String, List[UUID], Option[UUID], Option[UUID])].contramap(p =>
//      (p.id, p.name, p.effects, p.repeat, p.target))

  implicit val uuidListMeta: Meta[List[UUID]] =
    Meta.Advanced
      .other[PGobject]("json")
      .timap[List[UUID]](a => a.getValue.toJson.convertTo[List[UUID]])(
        a => {
          val o = new PGobject
          o.setType("json")
          o.setValue(a.map(_.toString).toJson.toString)
          o
        }
      )

  implicit val optionalUuidMeta: Meta[Option[UUID]] =
    Meta.Advanced
      .other[PGobject]("UUID")
      .timap[Option[UUID]](a =>
        a.getValue match {
          case null => None
          case s => Some(UUID.fromString(s))
      })(
        a => {
          val o = new PGobject
          o.setType("UUID")
          o.setValue(a.orNull.toString)
          o
        }
      )

}
