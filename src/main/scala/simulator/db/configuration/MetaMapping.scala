package simulator.db.configuration

import java.util.UUID

import doobie.postgres.implicits._
import doobie.util.{Meta, Read, Write}
import simulator.model._

trait MetaMapping {
  implicit val VarianceMeta = pgEnum(Variance, "Variance")
  implicit val ActionTypeMeta = pgEnum(ActionType, "type")
  implicit val EffectTypeMeta = pgEnum(EffectType, "type")

//  implicit val valueRead: Read[Value] = {
//    Read[(String, String)].map {
//      case (id, kind) => {
//        println(id)
//        println(kind)
//        kind match {
//          case "scalar" => Scalar(UUID.randomUUID(), Variance.None, 0, 0)
//          case "categorical" => Categorical(UUID.randomUUID(), Nil)
//        }
//      }
//    }
//  }
//
//  implicit val valueWrite: Write[Value] =
//    Write[Value].contramap {
//      case x: Scalar => x
//      case x: Categorical => x
//    }
}
