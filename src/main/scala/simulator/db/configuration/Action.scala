package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{ActionConfig, Config}
import simulator.db.PrimaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.model.ActionConfigData

class Action(override val tableName: String) extends PrimaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""

      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id  UUID NOT NUll,
        name text NOT NULL,
        action_enum text NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) =
    (sql"SELECT id, name, action_enum FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[ActionConfigData]
      .to[List]
      .transact(xa)

  override def write(instance: Config, configurationId: UUID): IO[Int] = {
    val model: ActionConfig = instance.asInstanceOf[ActionConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, action_enum)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.actionType})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }

}
