package util

import enumeratum._

sealed abstract class SiteEngine(val id: String, val title: String, val explain: Boolean = false) extends EnumEntry {
  override def toString = id
}

object SiteEngine extends Enum[SiteEngine] {
  case object DB2 extends SiteEngine(id = "db2", title = "DB2")
  case object H2 extends SiteEngine(id = "h2", title = "H2")
  case object Informix extends SiteEngine(id = "informix", title = "Informix")
  case object MySQL extends SiteEngine(id = "mysql", title = "MySQL", explain = true)
  case object Oracle extends SiteEngine(id = "oracle", title = "Oracle", explain = true)
  case object PostgreSQL extends SiteEngine(id = "postgres", title = "PostgreSQL", explain = true)
  case object SQLite extends SiteEngine(id = "sqlite", title = "SQLite")
  case object SQLServer extends SiteEngine(id = "sqlserver", title = "SQL Server")

  override val values = findValues
}
