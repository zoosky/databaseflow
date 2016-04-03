package ui

import java.util.UUID

import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

import scala.scalajs.js

object TabManager {
  private[this] var initialized = false
  private[this] var openTabs = Seq.empty[(UUID, String)]

  private[this] lazy val tabBar = $("#query-tabs")
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")

  def initIfNeeded() = if (!initialized) {
    $("#tab-loading").remove()
    initialized = true

    $("ul.tabs").on("click", "a", (e: JQueryEventObject) => {
      val queryId = UUID.fromString($(e.currentTarget).data("query").toString)
      openTabs.find(_._1 == queryId) match {
        case Some(x) =>
          org.scalajs.dom.document.location.hash = x._2
          utils.Logging.info("..." + queryId)
        case None => throw new IllegalStateException(s"No open tab [$queryId] from choices [${openTabs.mkString(", ")}].")
      }
    })
  }

  def addTab(id: UUID, ctx: String, title: String, icon: String) = {
    openTabs = openTabs :+ (id -> ctx)
    tabBar.append(s"""<li id="tab-$id" class="tab col s3">
      <a data-query="$id" href="#panel-$id"><i class="fa $icon"></i> $title</a>
    </li>""")
    $(s".tabs .indicator").remove()
    dynamicTabBar.tabs()

    selectTab(id)
  }

  def removeTab(queryId: UUID) = {
    openTabs = openTabs.filterNot(_._1 == queryId)
    $(s"#tab-$queryId").remove()
    $(s".tabs .indicator").remove()
    dynamicTabBar.tabs()
  }

  def selectTab(queryId: UUID) = {
    dynamicTabBar.tabs("select_tab", s"panel-$queryId")
  }
}
