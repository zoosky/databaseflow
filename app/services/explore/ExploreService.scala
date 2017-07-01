package services.explore

import models.connection.ConnectionSettings
import models.graphql.GraphQLContext
import sangria.schema._
import services.schema.SchemaService

import scala.concurrent.Await

object ExploreService {
  def exploreType(cs: ConnectionSettings) = {
    import scala.concurrent.duration._
    val f = SchemaService.getSchemaWithDetails(cs)
    val schema = Await.result(f, 120.seconds)

    val tables = ExploreTableHelper.getTables(schema)
    val views = ExploreViewHelper.getViews(schema)

    val exploreFields = (tables, views) match {
      case (Some(t), Some(v)) => fields[GraphQLContext, models.schema.Schema](
        Field(
          name = "table",
          fieldType = t,
          description = Some("This database's tables."),
          resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
        ),
        Field(
          name = "view",
          fieldType = v,
          description = Some("This database's views."),
          resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
        )
      )
      case (Some(t), None) => fields[GraphQLContext, models.schema.Schema](Field(
        name = "table",
        fieldType = t,
        description = Some("This database's tables."),
        resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
      ))
      case (None, Some(v)) => fields[GraphQLContext, models.schema.Schema](Field(
        name = "view",
        fieldType = v,
        description = Some("This database's views."),
        resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
      ))
      case (None, None) => fields[GraphQLContext, models.schema.Schema]()
    }

    ObjectType(name = "explore", description = "Explore this database's objects in a simple graph.", fields = exploreFields)
  }
}
