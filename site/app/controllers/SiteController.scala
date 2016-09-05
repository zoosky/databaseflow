package controllers

import akka.actor.ActorSystem
import com.codahale.metrics.SharedMetricRegistries
import play.api.Configuration
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, Controller}
import services.payment.StripePaymentService
import utils.metrics.{MetricsConfig, MetricsServletActor}

import scala.concurrent.Future

object SiteController {
  val cors = Seq(
    "Access-Control-Allow-Headers" -> "Content-Type,x-requested-with,Authorization,Access-Control-Allow-Origin",
    "Access-Control-Allow-Methods" -> "GET,POST,OPTIONS",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Max-Age" -> "360"
  )
}

@javax.inject.Singleton
class SiteController @javax.inject.Inject() (
    implicit
    val messagesApi: MessagesApi,
    val actorSystem: ActorSystem,
    val lifecycle: ApplicationLifecycle,
    val config: Configuration
) extends BaseSiteController {

  val metricsConfig = MetricsConfig(jmxEnabled = true, graphiteEnabled = false, "127.0.0.1", 2003, servletEnabled = true, 9001)
  actorSystem.actorOf(MetricsServletActor.props(metricsConfig), "metrics-servlet")
  lifecycle.addStopHook(() => Future.successful(SharedMetricRegistries.remove("default")))
  StripePaymentService.init(
    sk = config.getString("payment.sk").getOrElse(""),
    pk = config.getString("payment.pk").getOrElse(""),
    personalPrice = config.getInt("payment.price.personal").getOrElse(0),
    teamPrice = config.getInt("payment.price.team").getOrElse(0)
  )

  def splash() = act("splash") { implicit request =>
    Future.successful(Ok(views.html.splash()).withHeaders(SiteController.cors: _*))
  }

  def index() = act("index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.index(isAdmin)).withHeaders(SiteController.cors: _*))
  }

  def features() = act("features") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.features(isAdmin)))
  }

  def versions() = act("versions") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.versions(isAdmin)))
  }

  def screenshots() = act("screenshots") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.screenshots(isAdmin)))
  }

  def plan() = act("plan") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.plan(isAdmin)))
  }

  def language(lang: String) = act("language") { implicit request =>
    val l = Lang(lang)
    val result = Redirect(controllers.routes.SiteController.index()).withLang(l)
    Future.successful(if (lang == "en") {
      result
    } else {
      result.flashing("lang" -> lang)
    })

  }

  def database(key: String) = act(s"db.$key") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.database(key, isAdmin)))
  }

  def robots() = act("robots-txt") { implicit request =>
    Future.successful(Ok("User-agent: *\nDisallow:"))
  }
}
