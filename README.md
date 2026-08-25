# NSI requester

Detailed instructions can be found at: https://bandwidthondemand.github.io/nsi-requester/

Building a production instance: https://bandwidthondemand.github.io/nsi-requester/build

Installing software dependencies: https://bandwidthondemand.github.io/nsi-requester/thirdparty

How to front the nsi-requester with a reverse proxy: https://bandwidthondemand.github.io/nsi-requester/deployment

Start the application by `play run` or `sbt run`.
You need either Play! or sbt installed. Both available through [Homebrew][homebrew].

    brew install play
    git clone https://github.com/BandwidthOnDemand/nsi-requester.git
    cd nsi-requester
    play run

Move your browser to [http://localhost:9000](http://localhost:9000).

## Versioning

The release git tag is the only place a version is written by hand. sbt-dynver derives the version
from git, so a tag builds `2.1.0` and any other commit builds `2.1.0+3-g1a2b3c4`. `dynverVTagPrefix`
is off because release tags carry no `v` prefix. The version reaches the application through
sbt-buildinfo as `support.BuildInfo.version`, which the settings page displays.

The `Dockerfile` copies the whole tree, `.git` included, so no version needs to be passed in. Copy
only part of it and git reports the rest as deleted, which makes dynver stamp every build dirty. The
checkout must also be unshallow or dynver sees no tags, which is why
`.github/workflows/publish.yml` uses `fetch-depth: 0`. The chart takes the same tag as both its
version and its appVersion; see `.github/workflows/chart.yml`.

## Eclipse
For editing in Eclipse the [Scala IDE](http://scala-ide.org/) is very usefull.

Eclipse Juno users please use the follwoing update site:
[Eclipse IDE 2.1 milestones](http://download.scala-ide.org/releases-juno-29/milestone/site)

Generating the Eclipse project files can be done by running `eclipse with-source=true` in the sbt console.


[homebrew]: http://mxcl.github.com/homebrew
