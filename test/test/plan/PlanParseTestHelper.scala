package test.plan

import java.util.UUID

import models.engine.DatabaseEngine
import models.plan.{PlanNode, PlanResult}
import services.plan.PlanParseService
import util.Logging

import scala.io.Source

object PlanParseTestHelper extends Logging {
  private[this] def load(key: String) = {
    val sql = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"plan/$key.sql"))
    val plan = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"plan/$key.json"))

    val sqlContents = sql.getLines.toSeq.mkString("\n")
    val planContents = plan.getLines.toSeq.mkString("\n")

    sqlContents -> planContents
  }

  def test(key: String, engine: DatabaseEngine) = {
    val (sql, plan) = load(key)
    val queryId = UUID.randomUUID
    PlanParseService.parse(sql, queryId, plan, util.DateUtils.nowMillis)(engine) match {
      case Right(result) => result
      case Left(err) => throw new IllegalStateException(err.toString)
    }
  }

  def debugPlanResult(r: PlanResult) = {
    log.info(s"Plan output for [${r.queryId}]:")
    log.info(" Title                       | Relation           | Output             " +
      "| Type           | Props | ERows  | ARows  | ADur   | ACost  | ACostX ")
    log.info("-----------------------------|--------------------|--------------------" +
      "|----------------|-------|--------|--------|--------|--------|--------")
    debugNode(r.node, 0)
  }

  private[this] def debugNode(node: PlanNode, depth: Int): Unit = {
    val title = ((0 until depth).map(_ => "  ").mkString + node.title).padTo(27, ' ').substring(0, 27)
    val tp = node.nodeType.toString.padTo(14, ' ')
    val rel = node.relation.map(_.toString).getOrElse("").padTo(18, ' ').substring(0, 18)
    val output = node.output.map(_.mkString(", ")).getOrElse("").padTo(18, ' ').substring(0, 18)
    val props = node.properties.size.toString.padTo(5, ' ')
    val eRows = node.costs.estimatedRows.toString.padTo(6, ' ')
    val aRows = node.costs.actualRows.map(_.toString).getOrElse("").padTo(6, ' ')
    val aDur = node.costs.duration.map(_.toString).getOrElse("").padTo(6, ' ')
    val aCost = node.costs.cost.map(_.toString).getOrElse("").padTo(6, ' ')
    val aCostX = node.costWithoutChildren.map(_.toString).getOrElse("").padTo(6, ' ')
    log.info(s" $title | $rel | $output | $tp | $props | $eRows | $aRows | $aDur | $aCost | $aCostX")
    node.children.foreach(n => debugNode(n, depth + 1))
  }
}
