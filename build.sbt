// scalastyle:off

import sbt.IntegrationTest

name := "arq2"
organization := "com.github.leonhardtdavid"
scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin, BuildInfoPlugin, FlywayPlugin, GatlingPlugin)
  .configs(IntegrationTest, GTest)
  .settings(Defaults.itSettings, inConfig(GTest)(Defaults.testSettings))

lazy val GTest = config("gatling") extend Test
scalaSource in GTest := baseDirectory.value / "gatling" / "simulation"

buildInfoKeys := Seq[BuildInfoKey](name, version, BuildInfoKey.action("build_time") {
  java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now())
})
buildInfoPackage := "com.github.leonhardtdavid.arq2.info"
buildInfoObject := "BuildInfo"
buildInfoOptions += BuildInfoOption.ToJson

flywayUrl := "jdbc:postgresql://localhost/arq2?stringtype=unspecified"
flywayUser := "arq2"
flywayPassword := "arq2"
flywayLocations ++= Seq("migrations/ups", "migrations/downs")
flywayUrl in Test := "jdbc:h2:mem:arq2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE"

mappings in (Compile, packageDoc) := Seq()

libraryDependencies ++= {
  val circeVersion    = "0.11.1"
  val akkaVersion     = "2.5.26"
  val akkaHttpVersion = "10.1.11"
  val slickVersion    = "3.3.1"
  val gatlingVersion  = "3.3.1"

  Seq(
    "com.google.inject" % "guice"   % "4.2.2",
    "org.ehcache"       % "ehcache" % "3.7.1",
    // Logs
    "ch.qos.logback"    % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    // Json
    "io.circe"          %% "circe-core"           % circeVersion,
    "io.circe"          %% "circe-generic"        % circeVersion,
    "io.circe"          %% "circe-generic-extras" % circeVersion,
    "io.circe"          %% "circe-parser"         % circeVersion,
    "io.circe"          %% "circe-java8"          % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe"      % "1.27.0",
    "io.taig"           %% "circe-validation"     % "0.1.1",
    // Functional Programming
    "org.typelevel" %% "cats-core" % "1.6.1",
    // Authentication
    "org.mindrot"   % "jbcrypt"    % "0.4",
    "com.pauldijou" %% "jwt-circe" % "4.0.0",
    // Akka
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit"   % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http"      % akkaHttpVersion,
    // Database
    "com.typesafe.slick" %% "slick"          % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.postgresql"     % "postgresql"      % "42.2.6",
    // Monitoring
    "io.kamon" %% "kamon-bundle"       % "2.0.1",
    "io.kamon" %% "kamon-apm-reporter" % "2.0.0",
    // Testing
    "com.h2database"        % "h2"                        % "1.4.198"      % Test,
    "org.mockito"           % "mockito-core"              % "3.0.0"        % "it,test",
    "org.scalatest"         %% "scalatest"                % "3.0.8"        % "it,test",
    "com.dimafeng"          %% "testcontainers-scala"     % "0.29.0"       % "it,test",
    "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it",
    "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"
  )
}

//Esto es para que ande el debugger en test mode
fork in Test := false
parallelExecution in Test := false

fork in run := true

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views.
  "-language:postfixOps", // Allow postfix operations.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

// Warning suppression on autogenerated files
libraryDependencies ++= {
  val silencerVersion = "1.4.2"
  Seq(
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
    "com.github.ghik" %% "silencer-lib" % silencerVersion % Provided
  )
}
// silence all warnings on autogenerated files
scalacOptions += "-P:silencer:pathFilters=target/.*"
// Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"

compileOrder := CompileOrder.Mixed
javacOptions ++= Seq("-source", "1.8")

// Run checkstyle before compile
lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value
compileScalastyle := (compileScalastyle triggeredBy (scalafmt in Compile)).value

// Autoformat code configurations
scalafmtOnCompile := true
scalafmtTestOnCompile := true

// Coverage configurations
coverageMinimum := 90
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;router;global;xmls.*;"
coverageExcludedFiles := ".*BuildInfo.*;.*HealthCheckController.*;.*Routes.*"

dockerBaseImage := "openjdk:8-jre-alpine"
packageName in Docker := name.value
mainClass in Compile := Some("com.github.leonhardtdavid.arq2.Main")
