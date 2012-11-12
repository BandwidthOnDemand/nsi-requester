# NSI requester

Start the application by `play run` or `sbt run`.  
You need either Play! or sbt installed. Both available through [Homebrew][homebrew].

    brew install play
    git clone https://github.com/BandwidthOnDemand/nsi-requester.git
    cd nsi-requester
    play run

Move your browser to [http://localhost:9000](http://localhost:9000).

## Running the app on Heroku

You can only do this if you got the privileges on [Heroku][heroku] for this app. Or you could create your own [Heroku][heroku] application.

After you cloned this repository you need to run `heroku git:remote --app nsi-requester` to add the remote heroku git repo.  
The application on Heroku is configured by some env variables. You can list them by running `heroku config`. The vars override the properties in `application.properties` by setting them as system properties in the `Procfile`.

## Pusher

The application uses [Pusher][pusher] for async communication between the application and the client (browser). Because [Heroku][heroku] does not support websockets yet [Pusher][pusher] is used as an alternative.  
Default the application is configured with a pusher application (see `application.properties`, `pusher.appId`, `pusher.key` and `pusher.secret`) so it should run out of the box. But if you want to run this application as a service pleas create your own application on [Pusher][pusher].

## Eclipse
For editing in Eclipse the [Scala IDE](http://scala-ide.org/) is very usefull.

Eclipse Juno users please use the follwoing update site:
[Eclipse IDE 2.1 milestones](http://download.scala-ide.org/releases-juno-29/milestone/site)

Generating the Eclipse project files can be done by running `eclipse with-source=true` in the sbt console.


[heroku]: http://www.heroku.com
[pusher]: http://pusher.com
[homebrew]: http://mxcl.github.com/homebrew
