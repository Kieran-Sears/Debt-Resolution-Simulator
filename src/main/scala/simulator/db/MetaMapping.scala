package simulator.db

import doobie.util.Meta
import simulator.model._

trait MetaMapping {
  implicit val VarianceEnumMeta: Meta[VarianceEnum] = Meta[String].imap(VarianceEnum.fromEnum)(VarianceEnum.toEnum)
  implicit val ActionEnumMeta: Meta[ActionEnum] = Meta[String].imap(ActionEnum.fromEnum)(ActionEnum.toEnum)
  implicit val EffectEnumMeta: Meta[EffectEnum] = Meta[String].imap(EffectEnum.fromEnum)(EffectEnum.toEnum)
  implicit val AttributeEnumMeta: Meta[AttributeEnum] = Meta[String].imap(AttributeEnum.fromEnum)(AttributeEnum.toEnum)
}
