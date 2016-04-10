package models.template

import models.query.SavedQuery
import models.schema.{ Procedure, Table, View }
import utils.DomUtils

import scalatags.Text.all._

object SidenavTemplate {
  private[this] def savedQuery(sq: SavedQuery) = li(a(
    id := "saved-query-link-" + sq.id,
    cls := "sidenav-link waves-effect waves-light",
    data("key") := sq.id.toString,
    href := "#saved-query-" + sq.id,
    title := sq.name,
    data("name") := sq.name
  )(em(cls := s"fa ${Icons.savedQuery}"), span(sq.name)))
  def savedQueries(sqs: Seq[SavedQuery]) = sqs.map(savedQuery)

  private[this] def table(t: Table) = li(a(
    id := "table-link-" + DomUtils.cleanForId(t.name),
    cls := "sidenav-link waves-effect waves-light",
    data("key") := t.name,
    href := "#table-" + t.name,
    title := t.name,
    data("name") := t.name
  )(em(cls := s"fa ${Icons.tableClosed}"), span(t.name)))
  def tables(tables: Seq[Table]) = tables.map(table)

  private[this] def view(v: View) = li(a(
    id := "view-link-" + DomUtils.cleanForId(v.name),
    cls := "sidenav-link waves-effect waves-light",
    data("key") := v.name,
    href := "#view-" + v.name,
    title := v.name,
    data("name") := v.name
  )(em(cls := s"fa ${Icons.view}"), span(v.name)))
  def views(views: Seq[View]) = views.map(view)

  private[this] def procedure(p: Procedure) = li(a(
    id := "procedure-link-" + DomUtils.cleanForId(p.name),
    cls := "sidenav-link waves-effect waves-light",
    data("key") := p.name,
    href := "#procedure-" + p.name,
    title := p.name,
    data("name") := p.name
  )(em(cls := s"fa ${Icons.procedure}"), span(p.name)))
  def procedures(procedures: Seq[Procedure]) = procedures.map(procedure)
}
