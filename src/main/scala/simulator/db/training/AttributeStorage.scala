package simulator.db.training

import java.util.UUID
import cats.effect.IO
import doobie.implicits._
import simulator.model.Attribute
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class AttributeStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        customerId VARCHAR(36) NOT NULL,
        name text NOT NULL,
        value float NOT NULL
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

  def readById(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE id = ${id.toString}")
      .query[Attribute]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[Attribute]
      .to[List]
      .transact(xa)

  def write(model: Attribute, customerId: UUID) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, customerId, name, value)
          VALUES (${model.id}, ${customerId.toString} ${model.name}, ${model.value})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
}
