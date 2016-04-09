package services.schema

import java.util.UUID

import models.schema.Schema
import services.database.DatabaseConnection

object SchemaService {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty

  def getSchema(connectionId: UUID, db: DatabaseConnection, forceRefresh: Boolean = false) = schemaMap.get(connectionId) match {
    case Some(schema) if !forceRefresh => schema
    case None => calculateSchema(connectionId, db)
  }

  def refreshSchema(connectionId: UUID, db: DatabaseConnection) = schemaMap.get(connectionId) match {
    case Some(schema) => db.withConnection { conn =>
      val metadata = conn.getMetaData
      val updated = schema.copy(
        tables = MetadataTables.withTableDetails(db, conn, metadata, schema.tables),
        views = MetadataViews.withViewDetails(db, conn, metadata, schema.views),
        procedures = MetadataProcedures.withProcedureDetails(metadata, schema.catalog, schema.schemaName, schema.procedures),
        detailsLoadedAt = Some(System.currentTimeMillis)
      )
      schemaMap = schemaMap + (connectionId -> updated)
      updated
    }
    case None => throw new IllegalStateException(s"Attempted to refresh schema [$connectionId], which is not loaded.")
  }

  def getTable(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.views.find(_.name == name))
  def getProcedure(connectionId: UUID, name: String) = schemaMap.get(connectionId).flatMap(_.procedures.find(_.name == name))

  private[this] def calculateSchema(connectionId: UUID, db: DatabaseConnection) = db.withConnection { conn =>
    val catalogName = Option(conn.getCatalog)
    val schemaName = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = Schema(
      connectionId = connectionId,
      schemaName = schemaName,
      catalog = catalogName,
      url = metadata.getURL,
      username = metadata.getUserName,
      engine = db.engine.id,
      engineVersion = metadata.getDatabaseProductVersion,
      driver = metadata.getDriverName,
      driverVersion = metadata.getDriverVersion,
      catalogTerm = metadata.getCatalogTerm,
      schemaTerm = metadata.getSchemaTerm,
      procedureTerm = metadata.getProcedureTerm,
      maxSqlLength = metadata.getMaxStatementLength,

      tables = Nil,
      views = Nil,
      procedures = Nil
    )

    val tables = MetadataTables.getTables(db, conn, metadata, catalogName, schemaName)
    val views = MetadataViews.getViews(db, conn, metadata, catalogName, schemaName)
    val procedures = MetadataProcedures.getProcedures(metadata, catalogName, schemaName)

    val schema = schemaModel.copy(
      tables = tables,
      views = views,
      procedures = procedures
    )

    schemaMap = schemaMap + (connectionId -> schema)

    schema
  }
}
