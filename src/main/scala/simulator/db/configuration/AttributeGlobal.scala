package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.db.PrimaryConfigurationStorage
import simulator.db.model.AttributeConfigData
import simulator.model.{AttributeConfig, Config}
import doobie.postgres._
import doobie.postgres.implicits._

class AttributeGlobal(override val tableName: String) extends PrimaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id  UUID NOT NUll,
        name text NOT NULL,
        value UUID NOT NULL,
        attribute_type text NOT NULL
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  override def readByOwnerId(id: UUID) = {
    (sql"SELECT id, name, value, attribute_type FROM " ++ tableNameFragment ++ sql" WHERE configuration_id = $id")
      .query[AttributeConfigData]
      .to[List]
      .transact(xa)
  }

  override def write(config: Config, configurationId: UUID): IO[Int] = {
    val model: AttributeConfig = config.asInstanceOf[AttributeConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, name, value, attribute_type)
          VALUES (${model.id}, $configurationId, ${model.name}, ${model.value}, ${model.attributeType})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }

}
