package services.charting

import scala.scalajs.js

object ChartingTests {
  private[this] val baseOptions = js.Dynamic.literal(
    "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
  )

  def testLineChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32)
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }

  def testBubbleChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32),
        "mode" -> "markers",
        "marker" -> js.Dynamic.literal(
          "size" -> js.Array(10, 20, 30, 40, 50, 60)
        )
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }

  def testScatterChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32),
        "mode" -> "markers",
        "type" -> "scatter",
        "text" -> js.Array("One", "Two", "Three", "Four", "Five", "Six")
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }

  def test3DScatterChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32),
        "z" -> js.Array(1, 2, 4, 8, 16, 32),
        "mode" -> "markers",
        "type" -> "scatter3d"
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }

  def testBarChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array("One", "Two", "Three", "Four", "Five", "Six"),
        "y" -> js.Array(1, 2, 4, 8, 16, 32),
        "type" -> "bar"
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }

  def testPieChart(el: String) = {
    val data: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "values" -> js.Array(1, 2, 3, 4, 5, 6),
        "labels" -> js.Array("One", "Two", "Three", "Four", "Five", "Six"),
        "type" -> "pie"
      )
    )

    ChartingService.addChart(el, data, baseOptions)
  }
}
