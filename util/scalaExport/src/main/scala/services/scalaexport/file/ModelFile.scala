package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel

object ModelFile {
  def export(model: ExportModel) = {
    val root = if (model.scalaJs) { Some(ScalaFile.sharedSrc) } else { None }
    val file = ScalaFile(model.modelPackage, model.className, root = root)

    file.addImport("models.result.data", "DataField")
    file.add(s"object ${model.className} {", 1)
    file.add(s"val empty = ${model.className}(", 1)

    model.fields.foreach { field =>
      val value = field.t match {
        case ColumnType.BooleanType => field.defaultValue.map(v => if (v == "1" || v == "true") { "true" } else { "false" }).getOrElse("false")

        case ColumnType.ByteType => field.defaultValue.getOrElse("0")

        case ColumnType.IntegerType => field.defaultValue.getOrElse("0")
        case ColumnType.LongType => field.defaultValue.getOrElse("0") + "L"
        case ColumnType.ShortType => field.defaultValue.getOrElse("0") + ".toShort"
        case ColumnType.FloatType => field.defaultValue.getOrElse("0.0") + "f"
        case ColumnType.DoubleType => field.defaultValue.getOrElse("0.0")
        case ColumnType.BigDecimalType => s"BigDecimal(${field.defaultValue.getOrElse("0")})"

        case ColumnType.UuidType => field.defaultValue.map(d => s"UUID.fromString($d)").getOrElse("UUID.randomUUID")

        case ColumnType.TimestampType => "util.DateUtils.now"
        case ColumnType.DateType => "util.DateUtils.today"
        case ColumnType.TimeType => "util.DateUtils.currentTime"

        case _ => "\"" + field.defaultValue.getOrElse("") + "\""
      }

      val withOption = if (field.notNull) {
        value
      } else {
        s"Some($value)"
      }

      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }

      file.add(s"${field.propertyName} = $withOption$comma")
    }
    file.add(")", -1)
    file.add("}", -1)
    file.add()

    model.description.foreach(d => file.add(s"/** $d */"))

    if (model.scalaJs) {
      //file.add(s"""@scala.scalajs.js.annotation.JSExportTopLevel("${model.className}")""")
    }
    file.add(s"case class ${model.className}(", 1)
    addFields(model, file)

    model.extendsClass match {
      case Some(x) => file.add(") extends " + x + " {", -1)
      case None => file.add(") {", -1)
    }
    file.indent(1)
    file.add("def toDataFields = Seq(", 1)
    model.fields.foreach { field =>
      val x = if (field.notNull) {
        s"""DataField("${field.propertyName}", Some(${field.propertyName}.toString))"""
      } else {
        s"""DataField("${field.propertyName}", ${field.propertyName}.map(_.toString))"""
      }
      val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
      file.add(x + comma)
    }
    file.add(")", -1)
    file.add("}", -1)
    file
  }

  private[this] def addFields(model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.t.requiredImport.foreach(p => file.addImport(p, field.t.asScala))

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.forSqlType(field.sqlTypeName)
      case x => x.asScala
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    val propDefault = if (field.t == ColumnType.StringType) {
      if (field.notNull) {
        field.defaultValue.map(v => " = \"" + v + "\"").getOrElse("")
      } else {
        field.defaultValue.map(v => " = Some(\"" + v + "\")").getOrElse("")
      }
    } else {
      ""
    }
    val propDecl = s"${field.propertyName}: $propType$propDefault"
    val comma = if (model.fields.lastOption.contains(field)) { "" } else { "," }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(propDecl + comma)
  }
}
