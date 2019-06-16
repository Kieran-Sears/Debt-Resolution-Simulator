package simulator.db.configuration

import java.util.UUID
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import simulator.model.SimulationConfig
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor

class SimulationStorage(dbUrl: String, tableName: String, xa: Transactor[IO]) extends MetaMapping with LazyLogging {

  val tableNameFragment = Fragment.const(s"${tableName}SimulationConfig")

  def indexName(name: String) = Fragment.const(s"${tableName}_$name")

  def init(): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")
    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        id SERIAL PRIMARY KEY NOT NULL UNIQUE,
        startTime int NOT NULL,
        endTime int NOT NULL,
        numberOfCustomers int NOT NULL
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
      .query[SimulationConfig]
      .to[List]
      .transact(xa)

  def readAll() =
    (sql"SELECT * FROM " ++ tableNameFragment)
      .query[SimulationConfig]
      .to[List]
      .transact(xa)

  def write(model: SimulationConfig) =
    (sql"""INSERT INTO """ ++ tableNameFragment ++
      sql""" (id, startTime, endTime, numberOfCustomers)
          VALUES (${model.id.toString}, ${model.startTime}, ${model.endTime}, ${model.numberOfCustomers})
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)

}
