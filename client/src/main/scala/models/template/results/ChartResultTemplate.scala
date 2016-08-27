package models.template.results

import java.util.UUID

import utils.Messages

import scalatags.Text.all._

object ChartResultTemplate {
  def forChartResults(resultId: UUID) = div(id := s"chart-$resultId", cls := "results-chart-panel initially-hidden")(
    div(cls := "loading")(Messages("query.chart.loading")),
    div(cls := "chart-options-panel z-depth-1 initially-hidden"),
    div(cls := "chart-container initially-hidden")(
      div(cls := "chart-panel")
    )
  )
}