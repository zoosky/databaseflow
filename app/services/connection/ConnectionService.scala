package services.connection

import java.util.UUID

import akka.actor.{ ActorRef, Props }
import models._
import models.queries.query.SavedQueryQueries
import models.schema.Schema
import models.user.User
import services.database.{ MasterDatabase, SampleDatabaseService }
import services.schema.SchemaService
import utils.metrics.InstrumentedActor
import utils.{ Config, Logging }

import scala.util.{ Failure, Success }

object ConnectionService {
  def props(id: Option[UUID], supervisor: ActorRef, connectionId: UUID, user: Option[User], out: ActorRef, sourceAddress: String) = {
    Props(new ConnectionService(id.getOrElse(UUID.randomUUID), supervisor, connectionId, user, out, sourceAddress))
  }
}

class ConnectionService(
    val id: UUID = UUID.randomUUID,
    val supervisor: ActorRef,
    val connectionId: UUID,
    val user: Option[User],
    val out: ActorRef,
    val sourceAddress: String
) extends InstrumentedActor with StartHelper with DataHelper with TraceHelper with DetailHelper with QueryHelper with PlanHelper with SqlHelper with Logging {

  protected[this] var currentUsername = user.flatMap(_.username)
  protected[this] var userPreferences = user.map(_.preferences)
  protected[this] var dbOpt = attemptConnect()
  protected[this] val db = dbOpt.getOrElse(throw new IllegalStateException("Cannot connect to database."))

  protected[this] var schema: Option[Schema] = SchemaService.getSchema(db) match {
    case Success(s) => Some(s)
    case Failure(x) =>
      log.error("Unable to load schema.", x)
      out ! ServerError("SchemaLoadError", s"${x.getClass.getSimpleName} - ${x.getMessage}")
      None
  }
  protected[this] val savedQueries = MasterDatabase.conn.query(SavedQueryQueries.getForUser(user.map(_.id), connectionId))

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  override def preStart() = onStart()

  override def receiveRequest = {
    // Incoming basic messages
    case mr: MalformedRequest => timeReceive(mr) { log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].") }
    case p: Ping => timeReceive(p) { out ! Pong(p.timestamp) }
    case GetVersion => timeReceive(GetVersion) { out ! VersionResponse(Config.version) }
    case dr: DebugInfo => timeReceive(dr) { handleDebugInfo(dr.data) }

    case sq: SubmitQuery => timeReceive(sq) { handleSubmitQuery(sq.queryId, sq.sql, sq.action.getOrElse("run")) }
    case csd: CreateSampleDatabase => timeReceive(csd) { handleCreateSampleDatabase(csd.queryId) }

    case trd: GetTableRowData => timeReceive(trd) { handleGetTableRowData(trd.queryId, trd.name) }
    case vrd: GetViewRowData => timeReceive(vrd) { handleGetViewRowData(vrd.queryId, vrd.name) }

    case gtd: GetTableDetail => timeReceive(gtd) { handleGetTableDetail(gtd.name) }
    case gvd: GetViewDetail => timeReceive(gvd) { handleGetViewDetail(gvd.name) }
    case gpd: GetProcedureDetail => timeReceive(gpd) { handleGetProcedureDetail(gpd.name) }

    case qsr: QuerySaveRequest => timeReceive(qsr) { handleQuerySaveRequest(qsr.query) }
    case qdr: QueryDeleteRequest => timeReceive(qdr) { handleQueryDeleteRequest(qdr.id) }

    case im: InternalMessage => handleInternalMessage(im)
    case rm: ResponseMessage => out ! rm
    case x => throw new IllegalArgumentException(s"Unhandled message [${x.getClass.getSimpleName}].")
  }

  override def postStop() = {
    supervisor ! ConnectionStopped(id)
  }

  protected[this] def handleSubmitQuery(queryId: UUID, sql: String, action: String) = action match {
    case "run" => handleRunQuery(queryId, sql)
    case "explain" => handleExplainQuery(queryId, sql)
    case "analyze" => handleAnalyzeQuery(queryId, sql)
    case _ => throw new IllegalArgumentException(action)
  }

  protected[this] def handleCreateSampleDatabase(queryId: UUID) = {
    SampleDatabaseService(db, queryId, out)
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendConnectionTrace => timeReceive(ct) { handleConnectionTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
