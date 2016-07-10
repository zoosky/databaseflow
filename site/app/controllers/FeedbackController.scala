package controllers

import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{Action, Controller}
import services.feedback.FeedbackService
import services.feedback.FeedbackService.Feedback
import services.notification.NotificationService

import scala.concurrent.Future

object FeedbackController {
  val feedbackForm = Form(
    mapping(
      "id" -> uuid,
      "email" -> email,
      "content" -> nonEmptyText,
      "version" -> ignored(1),
      "created" -> ignored(new LocalDateTime())
    )(Feedback.apply)(Feedback.unapply)
  )
}

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (implicit val messagesApi: MessagesApi, notificationService: NotificationService) extends BaseSiteController {
  def feedbackForm() = act("feedback-form") { implicit request =>
    Future.successful(Ok(views.html.feedbackForm()))
  }

  def feedbackOptions() = act("feedback-options") { implicit request =>
    Future.successful(Ok("OK").withHeaders(SiteController.cors: _*))
  }

  def postFeedback(ajax: Boolean) = act("feedback-post") { implicit request =>
    val action = FeedbackController.feedbackForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.feedbackForm()),
      feedback => {
        FeedbackService.save(feedback)
        notificationService.onFeedbackSubmitted(feedback.id, feedback.from, feedback.content, feedback.occurred)

        if (ajax) {
          Ok(JsObject(Seq("status" -> JsString("OK"))))
        } else {
          Redirect(controllers.routes.SiteController.index()).flashing("success" -> "Thanks for your feedback!")
        }
      }
    )

    Future.successful(action.withHeaders(SiteController.cors: _*))
  }
}
