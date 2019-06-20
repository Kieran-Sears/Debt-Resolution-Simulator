package simulator.db.training

import java.util.UUID

import cats.effect.IO
import doobie.implicits._
import simulator.db.model.ActionData
import simulator.db.StorageImpl
import simulator.model.Action
import simulator.db.Storage
import doobie.postgres._
import doobie.postgres.implicits._

class ActionStorage(override val tableName: String) extends Storage {

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        name text NOT NULL,
        repeat VARCHAR(36),
        target VARCHAR(36)
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (id);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run.transact(xa)
    } yield queryResult

  def readById(id: UUID) =
    (sql"SELECT * FROM " ++ tableNameFragment ++ sql" WHERE id = ${id.toString}")
      .query[ActionData]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[ActionData]
      .to[List]
      .transact(xa)

  def write(model: Action) = {
    model.effects.foreach(StorageImpl.effectStorage.write(_, model.id))
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, name, repeat, target)
          VALUES (${model.id}, ${model.name}, ${model.repeat.map(_.id)}, ${model.target
        .map(_.id)})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
