// scalastyle:off

import com.github.leonhardtdavid.migrations.DatabaseConfig
import sbt.IntegrationTest

name := "arq2-http4s"
organization := "com.github.leonhardtdavid"
scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin, BuildInfoPlugin)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  BuildInfoKey.action("build_time") {
    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(java.time.LocalDateTime.now())
  }
)
buildInfoPackage := "com.github.leonhardtdavid.arq2.info"
buildInfoObject := "BuildInfo"
buildInfoOptions += BuildInfoOption.ToJson

migrationsConfigs := Seq(
  new DatabaseConfig(
    url = "jdbc:postgresql://localhost/arq2?stringtype=unspecified",
    user = Some("arq2"),
    password = Some("arq2")
  )
)

mappings in (Compile, packageDoc) := Seq()

libraryDependencies ++= {
  val http4sVersion  = "0.21.1"
  val circeVersion   = "0.13.0"
  val macwireVersion = "2.3.3"

  Seq(
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-circe"        % http4sVersion,
    "org.http4s" %% "http4s-dsl"          % http4sVersion,
    // Json
    "io.circe" %% "circe-generic"        % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion,
    // Dependency Injection
    "com.softwaremill.macwire" %% "macros"     % macwireVersion % Provided,
    "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % Provided,
    "com.softwaremill.macwire" %% "util"       % macwireVersion,
    "com.softwaremill.macwire" %% "proxy"      % macwireVersion,
    // Configurations
    "com.typesafe" % "config" % "1.4.0",
    // Cache
    "org.ehcache" % "ehcache" % "3.7.1",
    // Logs
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    // Authentication
    "org.mindrot"   % "jbcrypt"    % "0.4",
    "com.pauldijou" %% "jwt-circe" % "4.2.0",
    // Database
    "org.postgresql" % "postgresql" % "42.2.6",
    // Testing
    "org.scalatest" %% "scalatest"            % "3.0.8"  % "it,test",
    "com.dimafeng"  %% "testcontainers-scala" % "0.29.0" % "it,test"
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
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

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
