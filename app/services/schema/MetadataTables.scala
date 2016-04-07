package services.schema

import java.sql.{ Connection, DatabaseMetaData }

import models.database.{ Query, Row }
import models.schema.Table
import services.database.DatabaseConnection
import utils.NullUtils

object MetadataTables {
  def getTables(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
    val tables = new Row.Iter(rs).map(fromRow).toList.sortBy(_.name)
    tables.map { table =>
      getTableDetails(db, conn, metadata, table)
    }
  }

  private[this] def getTableDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, table: Table) = {
    val definition = if (db.engine.showCreateSupported) {
      Some(db(conn, new Query[String] {
        override def sql = db.engine.showCreateTable(table.name)
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create Table")).toList.head
      }))
    } else {
      None
    }

    table.copy(
      definition = definition,
      columns = MetadataColumns.getColumns(metadata, table),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, table),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table)
    )
  }

  def getViews(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("VIEW"))
    val views = new Row.Iter(rs).map(fromRow).toList.sortBy(_.name)
    views.map { table =>
      getViewDetails(db, conn, metadata, table)
    }
  }

  private[this] def getViewDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, view: Table) = {
    val definition = if (db.engine.showCreateSupported) {
      Some(db(conn, new Query[String] {
        override def sql = db.engine.showCreateView(view.name)
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create View")).toList.head
      }))
    } else {
      None
    }

    view.copy(
      definition = definition,
      columns = MetadataColumns.getColumns(metadata, view),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, view),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, view),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, view),
      indexes = MetadataIndexes.getIndexes(metadata, view)
    )
  }

  private[this] def fromRow(row: Row) = Table(
    name = row.as[String]("TABLE_NAME"),
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = None,
    typeName = row.as[String]("TABLE_TYPE")
  )
}
