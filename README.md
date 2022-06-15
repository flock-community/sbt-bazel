# sbt-bazel

## Install

project/plugins.sbt
```
externalResolvers := Seq(
  "Flock." at "https://flock.jfrog.io/artifactory/flock-sbt/"
)
addSbtPlugin("community.flock" % "sbt-bazel" % "0.1.4-SNAPSHOT")
```

## Run
```
sbt bazelInstall
```
