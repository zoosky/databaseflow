package models.queries

import models.database.{FlatSingleRowQuery, Query, Row, Statement}
import util.{Config, JdbcUtils}

trait BaseQueries[T] {
  protected def tableName: String = "_invalid_"
  protected def idColumns = Seq("id")
  protected def columns: Seq[String]
  protected lazy val quotedColumns = columns.map("\"" + _ + "\"").mkString(", ")
  protected lazy val columnPlaceholders = columns.map(_ => "?").mkString(", ")
  protected def searchColumns: Seq[String]

  protected def fromRow(row: Row): T
  protected def toDataSeq(t: T): Seq[Any]

  protected lazy val insertSql = s"""insert into "$tableName" ($quotedColumns) values ($columnPlaceholders)"""

  protected def updateSql(updateColumns: Seq[String], additionalUpdates: Option[String] = None) = {
    val updateCols = updateColumns.map(x => s""""$x" = ?""").mkString(", ")
    s"""update \"$tableName\" set $updateCols${additionalUpdates.fold("")(x => s", $x")} where $idWhereClause"""
  }

  protected def getSql(
    whereClause: Option[String] = None,
    groupBy: Option[String] = None,
    orderBy: Option[String] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None
  ) = {
    JdbcUtils.trim(s"""
      select ${columns.map("\"" + _ + "\"").mkString(", ")} from "$tableName"
      ${whereClause.map(x => s" where $x").getOrElse("")}
      ${groupBy.map(x => s" group by $x").getOrElse("")}
      ${orderBy.map(x => s" order by $x").getOrElse("")}
      ${limit.map(x => s" limit $x").getOrElse("")}
      ${offset.map(x => s" offset $x").getOrElse("")}
    """)
  }

  protected case class GetById(override val values: Seq[Any]) extends FlatSingleRowQuery[T] {
    override val sql = s"""select ${columns.map("\"" + _ + "\"").mkString(", ")} from "$tableName" where $idWhereClause"""
    override def flatMap(row: Row) = Some(fromRow(row))
  }

  protected case class GetAll(
      whereClause: Option[String] = None,
      orderBy: String = idColumns.map("\"" + _ + "\"").mkString(", "),
      override val values: Seq[Any] = Nil
  ) extends Query[Seq[T]] {
    override val sql = s"""
      select ${columns.map("\"" + _ + "\"").mkString(", ")}
      from "$tableName"
      ${whereClause.fold("")(w => "where " + w)}
      order by $orderBy
    """.trim
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  protected def getBySingleId(id: Any) = GetById(Seq(id))

  protected case class Insert(model: T) extends Statement {
    override val sql = insertSql
    override val values: Seq[Any] = toDataSeq(model)
  }

  protected case class RemoveById(override val values: Seq[Any]) extends Statement {
    override val sql = s"""delete from "$tableName" where $idWhereClause"""
  }

  protected case class Count(override val sql: String, override val values: Seq[Any] = Nil) extends Query[Int] {
    override def reduce(rows: Iterator[Row]) = rows.next().as[Long]("c").toInt
  }

  protected case class SearchCount(q: String, groupBy: Option[String] = None) extends Query[Int] {
    val searchWhere = if (q.isEmpty) { "" } else { "where " + searchColumns.map(c => s"""lower("$c") like lower(?)""").mkString(" or ") }
    override val sql = s"""select count(*) as c from "$tableName" $searchWhere ${groupBy.fold("")(x => s" group by $x")}"""
    override def reduce(rows: Iterator[Row]) = rows.next().as[Long]("c").toInt
  }
  protected case class Search(q: String, orderBy: String, page: Option[Int], groupBy: Option[String] = None) extends Query[List[T]] {
    private[this] val whereClause = if (q.isEmpty) { None } else { Some(searchColumns.map(c => s"lower($c) like lower(?)").mkString(" or ")) }
    private[this] val limit = page.map(_ => Config.pageSize)
    private[this] val offset = page.map(x => x * Config.pageSize)
    override val sql = getSql(whereClause, groupBy, Some(orderBy), limit, offset)
    override val values = if (q.isEmpty) { Seq.empty } else { searchColumns.map(_ => s"%$q%") }
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  private def idWhereClause = idColumns.map(c => s""""$c" = ?""").mkString(" and ")
}
