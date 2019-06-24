package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{CategoricalConfig, Config}
import simulator.db.SecondaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.model.CategoricalConfigData

class Categorical(override val tableName: String) extends SecondaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id UUID NOT NULL,
        attribute_configuration_id UUID NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) = {
    println("catC store read")
    (sql"SELECT id FROM " ++ tableNameFragment ++ sql" WHERE attribute_configuration_id = $id")
      .query[CategoricalConfigData]
      .unique
      .transact(xa)
  }

  override def write(config: Config, configurationId: UUID, attributeId: UUID) = {
    val model = config.asInstanceOf[CategoricalConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, attribute_configuration_id)
          VALUES (${model.id}, $configurationId, $attributeId)
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
