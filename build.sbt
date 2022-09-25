name := "Crawler"

version := "0.1"

scalaVersion := "2.13.9"

val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.0-M5"
val CatsEffectVersion = "3.3.14"
val ScalaScraperVersion = "3.0.0"

libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats-effect" % CatsEffectVersion,
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "org.http4s"      %% "http4s-scala-xml"    % Http4sVersion,
  "io.circe"        %% "circe-generic"       % CirceVersion,
  "net.ruippeixotog" %% "scala-scraper" % ScalaScraperVersion,
  "org.scalatest" %% "scalatest" % "3.2.13" % "test"

)



addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

