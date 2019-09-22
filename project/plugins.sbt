// Application start and stop management
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Migrations manager
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "6.0.0")

// Version management plugin
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

// Build info helper for health check
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

// Build tools
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")

// For those who still wants to use eclipse IDE...
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// Coverage report
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

// Check code style
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
// Autoformat code
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")
