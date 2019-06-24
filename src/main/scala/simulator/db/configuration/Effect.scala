package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{Config, EffectConfig}
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.SecondaryConfigurationStorage

class Effect(override val tableName: String) extends SecondaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        action_configuration_id UUID NOT NULL,
        name text NOT NULL,
        effect_type text NOT NULL,
        target text NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) =
    (sql"SELECT id, name, effect_type, target FROM " ++ tableNameFragment ++ sql" WHERE action_configuration_id = $id")
      .query[EffectConfig]
      .to[List]
      .transact(xa)

  override def write(config: Config, configurationId: UUID, actionId: UUID) = {
    val model = config.asInstanceOf[EffectConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, action_configuration_id, name, effect_type, target)
          VALUES (${model.id}, $configurationId, $actionId, ${model.name}, ${model.effectType}, ${model.target})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
