package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.ActionConfig
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class ActionConfigurationStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""

      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id  UUID NOT NUll,
        name text NOT NULL,
        action_enum text NOT NULL,
        effect_configurations VARCHAR[] NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run
        .transact(xa)
    } yield queryResult

  def readByConfigurationId(id: UUID) =
    (sql"SELECT (id, name, action_enum, effect_configurations) FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[ActionConfig]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[ActionConfig]
      .to[List]
      .transact(xa)

  def write(model: ActionConfig, configurationId: UUID) = {
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, action_enum, effect_configurations)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.actionType}, ${model.effectConfigurations})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
