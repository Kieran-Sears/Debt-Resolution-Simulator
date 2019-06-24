package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{Config, OptionConfig}
import simulator.db.SecondaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.model.OptionConfigData

class Option(override val tableName: String) extends SecondaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        categorical_configuration_id UUID NOT NULL,
        name text NOT NULL,
        probability INTEGER NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) =
    (sql"SELECT id, name, probability FROM " ++ tableNameFragment ++ sql" WHERE categorical_configuration_id = $id")
      .query[OptionConfigData]
      .to[List]
      .transact(xa)

  override def write(config: Config, configurationId: UUID, categoricalId: UUID) = {
    val model = config.asInstanceOf[OptionConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, categorical_configuration_id, name, probability)
          VALUES (${model.id}, $configurationId, $categoricalId, ${model.name}, ${model.probability})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
