package simulator.db.configuration

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.model.{AttributeConfig, Config}
import simulator.db.SecondaryConfigurationStorage
import doobie.postgres._
import doobie.postgres.implicits._
import simulator.db.model.AttributeConfigData

class Attribute(override val tableName: String) extends SecondaryConfigurationStorage {

  override def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id UUID PRIMARY KEY NOT NULL UNIQUE,
        configuration_id  UUID NOT NUll,
        customer_configuration_id UUID NOT NULL,
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
    println("attC store read")
    (sql"SELECT id, name, value, attribute_type FROM " ++ tableNameFragment ++ sql" WHERE customer_configuration_id = $id")
      .query[AttributeConfigData]
      .to[List]
      .transact(xa)
  }

  override def write(config: Config, configurationId: UUID, customerId: UUID): IO[Int] = {
    val model: AttributeConfig = config.asInstanceOf[AttributeConfig]
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, configuration_id, customer_configuration_id, name, value, attribute_type)
          VALUES (${model.id}, $configurationId, $customerId, ${model.name}, ${model.value}, ${model.attributeType})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
