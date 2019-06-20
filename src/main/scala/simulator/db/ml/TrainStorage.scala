package simulator.db.ml

import cats.effect.IO
import doobie.implicits._
import doobie.util.fragment.Fragment
import simulator.db.Storage
import simulator.model.{AttributeConfig, Customer}
import doobie.postgres._
import doobie.postgres.implicits._

class TrainStorage(override val tableName: String) extends Storage {

  def init(attributes: List[AttributeConfig]): IO[Int] = {
    logger.info(s"Initiliasing Database Table $tableName at $dbUrl")

    val fields = Fragment.const(attributes.foldLeft("") { case (acc, a) => acc + s"${a.name} DOUBLE NOT NULL, " })

    for {
      queryResult <- (sql"""
      CREATE TABLE IF NOT EXISTS """ ++ tableNameFragment ++
        sql""" (
        customerId VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
        """ ++ fields ++ sql"""
        actionName
      );
      CREATE INDEX IF NOT EXISTS """ ++ indexName("to") ++ sql" ON " ++ tableNameFragment ++
        sql""" (customerId);
    """).update.run.transact(xa)
    } yield queryResult
  }

  def drop(): IO[Int] =
    for {
      queryResult <- (sql"DROP TABLE IF EXISTS " ++ tableNameFragment ++ sql";").update.run.transact(xa)
    } yield queryResult

  def write(customer: Customer, actionName: String) = {
    val names =
      Fragment.const(
        customer.featureValues.foldLeft(" (customerId, ") { case (acc, a) => acc + s"${a.name}, " } + " actionName)")
    val values =
      Fragment.const(s"${customer.id.toString}, ${customer.featureValues.map(a => a.value + ", ")} $actionName")
    (sql"""INSERT INTO """ ++ tableNameFragment ++ names ++
      sql""" VALUES (""" ++ values ++ sql""")
          ON CONFLICT ON CONSTRAINT """ ++ indexName("pkey") ++
      sql""" DO NOTHING""").update.run
      .transact(xa)
  }
}
