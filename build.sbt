libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.0-MF", 
  "org.typelevel" %% "cats-free" % "1.0.0-MF",
  "org.typelevel" %% "cats-effect" % "0.4",
  "com.chuusai" %% "shapeless" % "2.3.2"
)

scalaVersion := "2.12.4"
scalacOptions ++= Seq("-unchecked", "-deprecation")
