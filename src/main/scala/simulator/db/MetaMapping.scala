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

//  implicit val listUuidRead: Read[List[UUID]] =
//    Read[List[String]].map { a =>
//      a.map(UUID.fromString)
//    }
//
//  implicit val listUuidPut: Put[List[UUID]] =
//    Put[List[String]].contramap { a =>
//      a.map(x => x.toString)
//    }
//
//  implicit val optionUuidRead: Read[Option[UUID]] =
//    Read[String].map {
//      case s: String => Some(UUID.fromString(s))
//      case _ => None
//    }
//
//  implicit val optionUuidPut: Put[Option[UUID]] =
//    Put[String].contramap {
//      case Some(uuid) => uuid.toString
//      case None => ""
//    }
//
//  implicit val uuidListMeta: Meta[List[UUID]] =
//    Meta.Advanced
//      .other[PGobject]("VARCHAR[]")
//      .timap[List[UUID]](a => a.getValue.toJson.convertTo[List[UUID]])(
//        a => {
//          val o = new PGobject
//          o.setType("VARCHAR[]")
//          o.setValue(a.map(_.toString).toJson.toString)
//          o
//        }
//      )
//
//  implicit val optionalUuidMeta: Meta[Option[UUID]] =
//    Meta.Advanced
//      .other[PGobject]("UUID")
//      .timap[Option[UUID]](a =>
//        a.getValue match {
//          case null => None
//          case s => Some(UUID.fromString(s))
//      })(
//        a => {
//          val o = new PGobject
//          o.setType("UUID")
//          o.setValue(a.orNull.toString)
//          o
//        }
//      )

}
