package ui.query

import java.util.UUID

import models.GetProcedureDetail
import models.schema.Procedure
import models.template.Icons
import models.template.proc.{ProcedureDetailTemplate, ProcedureParameterDetailTemplate}
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import ui.metadata.MetadataManager
import ui.{TabManager, WorkspaceManager}
import utils.{Logging, NetworkMessage, TemplateUtils}

object ProcedureManager {
  var openProcedures = Map.empty[String, UUID]

  def addProcedure(p: Procedure) = {
    openProcedures.get(p.name).foreach { uuid =>
      setProcedureDetails(uuid, p)
    }
  }

  def procedureDetail(name: String) = openProcedures.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      WorkspaceManager.append(ProcedureDetailTemplate.forProcedure(queryId, name).toString)

      MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
        case Some(procedure) if procedure.params.nonEmpty => setProcedureDetails(queryId, procedure)
        case _ => NetworkMessage.sendMessage(GetProcedureDetail(name))
      }

      def close() = {
        openProcedures = openProcedures - name
        QueryManager.closeQuery(queryId)
      }

      TabManager.addTab(queryId, "procedure-" + name, name, Icons.procedure, close)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      TemplateUtils.clickHandler($(".call-link", queryPanel), jq => {
        MetadataManager.schema.flatMap(_.procedures.find(_.name == name)) match {
          case Some(procedure) => callProcedure(queryId, procedure)
          case None => NotificationService.info("Procedure Not Loaded", "Please retry in a moment.")
        }
      })

      openProcedures = openProcedures + (name -> queryId)
  }

  private[this] def callProcedure(queryId: UUID, procedure: Procedure) = {
    Logging.debug(s"Calling procedure [$queryId]: " + procedure)
    // NetworkMessage.sendMessage(???)
  }

  private[this] def setProcedureDetails(uuid: UUID, proc: Procedure) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing procedure panel for [$uuid].")
    }

    proc.description.map { desc =>
      $(".description", panel).text(desc)
    }

    Logging.debug(s"Procedure [${proc.name}] loaded.")

    if (proc.params.nonEmpty) {
      val section = $(".params-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(proc.params.size.toString)
      $(".section-content", section).html(ProcedureParameterDetailTemplate.paramsPanel(proc.params).render)
    }

    scalajs.js.Dynamic.global.$(".collapsible", panel).collapsible()
  }
}
