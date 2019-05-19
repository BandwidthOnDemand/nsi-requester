# NSI requester

Detailed instructions can be found at: http://bandwidthondemand.github.io/nsi-requester/

Building a production instance: http://bandwidthondemand.github.io/nsi-requester/build

Installing software dependencies: http://bandwidthondemand.github.io/nsi-requester/thirdparty

How to front the nsi-requester with a reverse proxy: http://bandwidthondemand.github.io/nsi-requester/deployment

Start the application by `play run` or `sbt run`.  
You need either Play! or sbt installed. Both available through [Homebrew][homebrew].

    brew install play
    git clone https://github.com/BandwidthOnDemand/nsi-requester.git
    cd nsi-requester
    play run

Move your browser to [http://localhost:9000](http://localhost:9000).

## Eclipse
For editing in Eclipse the [Scala IDE](http://scala-ide.org/) is very usefull.

Eclipse Juno users please use the follwoing update site:
[Eclipse IDE 2.1 milestones](http://download.scala-ide.org/releases-juno-29/milestone/site)

Generating the Eclipse project files can be done by running `eclipse with-source=true` in the sbt console.


[homebrew]: http://mxcl.github.com/homebrew
