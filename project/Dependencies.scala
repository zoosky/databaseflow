import sbt._

object Dependencies {
  object Cache {
    val ehCache = "net.sf.ehcache" % "ehcache-core" % "2.6.11"
  }

  object Logging {
    val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.21"
  }

  object Play {
    private[this] val version = "2.5.8"
    val playLib = "com.typesafe.play" %% "play" % version
    val playFilters = play.sbt.PlayImport.filters
    val playWs = play.sbt.PlayImport.ws
    val playTest = "com.typesafe.play" %% "play-test" % version % "test"
    val playMailer = "com.typesafe.play" %% "play-mailer" % "5.0.0"
  }

  object Akka {
    private[this] val version = "2.4.10"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
  }

  object Authentication {
    private[this] val version = "4.0.0"
    val silhouette = "com.mohiva" %% "play-silhouette" % version
    val hasher = "com.mohiva" %% "play-silhouette-password-bcrypt" % version
    val persistence = "com.mohiva" %% "play-silhouette-persistence" % version
    val crypto = "com.mohiva" %% "play-silhouette-crypto-jca" % version
  }

  object Jdbc {
    val hikariCp = "com.zaxxer" % "HikariCP" % "2.5.1"

    val db2 = "dblibs/lib/db2-db2jcc4.jar"
    val h2 = "com.h2database" % "h2" % "1.4.192"
    val informix = "dblibs/lib/informix-ifxjdbc.jar"
    val mysql = "mysql" % "mysql-connector-java" % "5.1.39" // 6.0 is all different
    val postgres = "org.postgresql" % "postgresql" % "9.4.1211"
    val oracle = "dblibs/lib/oracle-ojdbc7.jar"
    val sqlite = "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
    val sqlServer = "dblibs/lib/sqlserver-sqljdbc42.jar"
  }

  object Hibernate {
    val core = "org.hibernate" % "hibernate-core" % "5.1.0.Final"
  }

  object Export {
    val csv = "com.github.tototoshi" %% "scala-csv" % "1.3.3"
    val xlsx = "org.apache.poi" % "poi-ooxml" % "3.14"
  }

  object Ui {
    val swing = "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"
  }

  object Serialization {
    val version = "0.4.2"
    val uPickle = "com.lihaoyi" %% "upickle" % version
  }

  object WebJars {
    val fontAwesome = "org.webjars" % "font-awesome" % "4.6.3"
    val jquery = "org.webjars" % "jquery" % "2.2.4"
    val materialize = "org.webjars" % "materializecss" % "0.97.7"
    val moment = "org.webjars" % "momentjs" % "2.15.0"
    val mousetrap = "org.webjars" % "mousetrap" % "1.6.0"
    val plotly = "org.webjars.bower" % "plotly.js" % "1.17.2"
  }

  object Metrics {
    val metrics = "nl.grons" %% "metrics-scala" % "3.5.5"
    val jvm = "io.dropwizard.metrics" % "metrics-jvm" % "3.1.2"
    val ehcache = "io.dropwizard.metrics" % "metrics-ehcache" % "3.1.2" intransitive()
    val healthChecks = "io.dropwizard.metrics" % "metrics-healthchecks" % "3.1.2" intransitive()
    val json = "io.dropwizard.metrics" % "metrics-json" % "3.1.2"
    val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "9.3.11.v20160721"
    val servlets = "io.dropwizard.metrics" % "metrics-servlets" % "3.1.2" intransitive()
    val graphite = "io.dropwizard.metrics" % "metrics-graphite" % "3.1.2" intransitive()
  }

  object Commerce {
    val stripeVersion = "2.8.0"
    val stripe = "com.stripe" % "stripe-java" % stripeVersion
  }

  object Utils {
    val scapegoatVersion = "1.2.1"
    val enumeratumVersion = "1.4.14"

    val commonsIo = "commons-io" % "commons-io" % "2.5"
    val crypto = "xyz.wiedenhoeft" %% "scalacrypt" % "0.4.0"
    val enumeratum = "com.beachape" %% "enumeratum-upickle" % enumeratumVersion
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.1.0"
  }

  object Testing {
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    val gatlingCore = "io.gatling" % "gatling-test-framework" % "2.1.7" % "test"
    val gatlingCharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7" % "test"
  }
}
