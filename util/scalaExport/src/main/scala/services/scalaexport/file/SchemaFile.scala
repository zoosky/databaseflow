package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object SchemaFile {
  val resultArgs = "paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(model.modelPackage, model.className + "Schema")

    file.addImport("util.FutureUtils", "graphQlContext")
    SchemaHelper.addImports(file)

    file.add(s"""object ${model.className}Schema extends SchemaHelper("${model.propertyName}") {""", 1)
    SchemaHelper.addPrimaryKey(model, file)
    SchemaHelper.addPrimaryKeyArguments(model, file)
    ForeignKeysHelper.writeSchema(config, model, file)
    addObjectType(config, model, file)
    addQueryFields(model, file)
    SchemaMutationHelper.addMutationFields(model, file)
    file.add()
    file.add(s"private[this] def toResult(r: SearchResult[${model.className}]) = {", 1)
    file.add(s"${model.className}Result($resultArgs)")
    file.add("}", -1)
    file.add("}", -1)
    file
  }

  private[this] def addObjectType(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val columnsDescriptions = model.fields.flatMap(col => col.description.map(d => s"""DocumentField("${col.propertyName}", "$d")"""))
    if (columnsDescriptions.isEmpty && model.foreignKeys.isEmpty && model.references.isEmpty) {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType()")
    } else {
      file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
      model.description.foreach {
        case d if columnsDescriptions.isEmpty && model.references.isEmpty && model.foreignKeys.isEmpty => file.add(s"""ObjectTypeDescription("$d")""")
        case d => file.add(s"""ObjectTypeDescription("$d"),""")
      }
      columnsDescriptions.foreach {
        case d if columnsDescriptions.lastOption.contains(d) && model.references.isEmpty => file.add(d)
        case d => file.add(d + ",")
      }
      if (model.foreignKeys.nonEmpty || model.references.nonEmpty) {
        file.add("AddFields(", 1)
      }
      ReferencesHelper.writeFields(config, model, file)
      ForeignKeyFields.writeFields(config, model, file)
      if (model.foreignKeys.nonEmpty || model.references.nonEmpty) {
        file.add(")", -1)
      }
      file.add(")", -1)
    }
    file.add()
    file.add(s"implicit lazy val ${model.propertyName}ResultType: ObjectType[GraphQLContext, ${model.className}Result] = deriveObjectType()")
    file.add()
  }

  private[this] def addQueryFields(model: ExportModel, file: ScalaFile) = {
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)

    file.add(s"""name = "${model.propertyName}",""")
    file.add(s"fieldType = ${model.propertyName}ResultType,")
    file.add(s"arguments = queryArg :: reportFiltersArg :: orderBysArg :: limitArg :: offsetArg :: Nil,")

    val args = s"td => runSearch(c.ctx.${model.serviceReference}, c)(td).map(toResult)"
    file.add(s"""resolve = c => trace(c.ctx, "search")($args)""")

    file.add("))", -1)
  }
}
