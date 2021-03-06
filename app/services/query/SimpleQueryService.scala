package services.query

import java.util.UUID

import models._
import models.audit.AuditType
import models.connection.ConnectionSettings
import models.database.Queryable
import models.queries.dynamic.DynamicQuery
import models.query.QueryResult
import models.user.User
import util.FutureUtils.defaultContext
import services.audit.AuditRecordService
import services.database.DatabaseRegistry
import util.{DateUtils, Logging}

import scala.concurrent.Future
import scala.util.control.NonFatal

object SimpleQueryService extends Logging {
  def run(db: Queryable, sql: String, user: UUID, connectionId: UUID) = {
    val startMs = DateUtils.nowMillis
    val auditId = UUID.randomUUID
    val resultId = UUID.randomUUID
    val queryId = UUID.randomUUID

    log.info(s"Performing simple query with resultId [$resultId] for query [$queryId] with sql [$sql].")
    Future(AuditRecordService.start(auditId, AuditType.Query, user, Some(connectionId), Some(sql)))

    val result = db.query(DynamicQuery(sql, Nil))

    val elapsedMs = (DateUtils.nowMillis - startMs).toInt
    val qr = QueryResult(
      queryId = queryId, sql = sql, columns = result.cols, data = result.data, rowsAffected = result.data.length, elapsedMs = elapsedMs, occurred = startMs
    )
    QueryResultResponse(resultId, 0, qr)
  }

  def runQuery(user: User, cs: ConnectionSettings, sql: String) = DatabaseRegistry.databaseForUser(user, cs.id) match {
    case Right(conn) => SimpleQueryService.run(conn, sql, user.id, conn.connectionId)
    case Left(ex) => throw ex
  }

  def runQueryWithCatch(user: User, cs: ConnectionSettings, sql: String) = try {
    runQuery(user, cs, sql)
  } catch {
    case NonFatal(x) => QueryResultResponse(UUID.randomUUID, 0, QueryResult.error(UUID.randomUUID, sql, x))
  }
}
