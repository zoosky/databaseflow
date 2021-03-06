package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlFormFile {
  def export(model: ExportModel) = {
    val file = TwirlFile(model.viewPackage, model.propertyName + "Form")

    val interpArgs = model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ")
    val viewArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")

    file.add(s"@(user: models.user.User, model: ${model.modelClass}, title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@layout.admin(user, "explore", title) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("<div class=\"collection-header\">", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add(s"""<div class="right"><a href="@cancel" class="theme-text cancel-link">Cancel</a></div>""")
    file.add(s"""<h5><i class="fa @models.template.Icons.${model.propertyName}"></i> @title</h5>""")
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table class=\"highlight\">", 1)
    file.add("<tbody>", 1)

    model.fields.foreach { field =>
      file.add("<tr>", 1)
      file.add("<td>", 1)
      val inputProps = s"""type="checkbox" name="${field.propertyName}.include" id="${field.propertyName}.include" value="true""""
      val dataProps = s"""class="data-input" data-type="${field.t}" data-name="${field.propertyName}""""
      if (field.notNull) {
        file.add(s"""<input $inputProps @if(isNew) { checked="checked" } $dataProps />""")
      } else {
        file.add(s"""<input $inputProps $dataProps />""")
      }
      file.add(s"""<label for="${field.propertyName}.include">${field.title}</label>""")
      file.add("</td>", -1)

      file.add("<td>", 1)
      TwirlFormFields.inputFor(field, file)
      file.add(s"</td>", -1)
      file.add("</tr>", -1)
    }

    file.add("</tbody>", -1)
    file.add("</table>", -1)
    file.add("</div>", -1)

    file.add("</div>", -1)
    file.add("</form>", -1)

    file.add("}", -1)

    file.add("@views.html.components.includeScalaJs(debug)")
    file.add(s"""<script>$$(function() { new FormService('form-edit-${model.propertyName}'); })</script>""")

    file
  }
}
