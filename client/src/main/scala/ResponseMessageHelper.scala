import models._
import services._
import ui.{HistoryManager, UserManager}
import utils.Logging

trait ResponseMessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {

    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)

    case us: UserSettings => UserManager.onUserSettings(us)

    case sqrr: SavedQueryResultResponse => ModelResultsService.handleSavedQueryResponse(sqrr)

    case srr: SchemaResultResponse => ModelResultsService.handleSchemaResultResponse(srr)
    case trr: TableResultResponse => ModelResultsService.handleTableResultResponse(trr.tables)
    case vrr: ViewResultResponse => ModelResultsService.handleViewResultResponse(vrr.views)
    case prr: ProcedureResultResponse => ModelResultsService.handleProcedureResultResponse(prr.procedures)

    case arr: AuditRecordResponse => HistoryManager.handleAuditHistoryResponse(arr.history)
    case arr: AuditRecordRemoved => HistoryManager.handleAuditHistoryRemoved(arr.id)

    case ts: TransactionStatus => utils.Logging.info("TransactionStatus: " + ts)

    case qcr: QueryCheckResponse => QueryErrorService.handleQueryCheckResponse(qcr)
    case qrr: QueryResultResponse => if (qrr.result.source.exists(_.dataOffset > 0)) {
      QueryAppendService.handleAppendQueryResult(qrr.id, qrr.result)
    } else {
      QueryResultService.handleNewQueryResults(qrr.id, qrr.result, qrr.durationMs)
    }
    case qrrc: QueryResultRowCount => QueryResultService.handleResultRowCount(qrrc)
    case qer: QueryErrorResponse => QueryErrorService.handleQueryErrorResponse(qer)

    case prr: PlanResultResponse => QueryPlanService.handlePlanResultResponse(prr)
    case per: PlanErrorResponse => QueryPlanService.handlePlanErrorResponse(per)

    case bqs: BatchQueryStatus => ModelResultsService.handleBatchQueryStatus(bqs)

    case qsr: QuerySaveResponse => ModelResultsService.handleQuerySaveResponse(qsr.savedQuery, qsr.error)
    case qdr: QueryDeleteResponse => ModelResultsService.handleQueryDeleteResponse(qdr.id, qdr.error)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }
}