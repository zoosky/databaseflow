package controllers

import java.util.UUID

import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.forms.ConnectionForm
import services.connection.ConnectionSettingsService
import services.database.DatabaseRegistry
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionTestController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def test(connectionId: UUID) = withSession("connection.test") { implicit request =>
    val result = ConnectionForm.form.bindFromRequest.fold(
      formWithErrors => {
        val errors = utils.web.FormUtils.errorsToString(formWithErrors.errors)
        BadRequest(s"Invalid form: $errors")
      },
      cf => {
        val almostUpdated = ConnectionSettings(
          engine = DatabaseEngine.get(cf.engine),
          host = if (cf.host.trim.isEmpty) { None } else { Some(cf.host) },
          port = cf.port,
          dbName = if (cf.dbName.trim.isEmpty) { None } else { Some(cf.dbName) },
          extra = if (cf.extra.trim.isEmpty) { None } else { Some(cf.extra) },
          urlOverride = if (cf.urlOverride.trim.isEmpty) { None } else { Some(cf.urlOverride) },
          username = cf.username
        )
        val updated = if (cf.password.trim.isEmpty) {
          val connOpt = ConnectionSettingsService.getById(connectionId)
          val conn = connOpt match {
            case Some(c) => c
            case None => ConnectionSettings(id = connectionId)
          }
          almostUpdated.copy(password = conn.password)
        } else {
          almostUpdated.copy(password = cf.password)
        }
        val result = DatabaseRegistry.connect(updated, 1)
        result match {
          case Right(x) =>
            x._1.close()
            Ok("ok: " + x._2)
          case Left(x) => Ok("error: " + x.getMessage)
        }
      }
    )
    Future.successful(result)
  }
}
