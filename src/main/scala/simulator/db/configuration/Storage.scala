package simulator.db.configuration

import doobie.LogHandler
import com.typesafe.scalalogging.LazyLogging

trait Storage extends MetaMapping with LazyLogging {
  implicit val han: LogHandler = LogHandler.jdkLogHandler
  // todo reduce repeated code by moving common functionality into here
}
