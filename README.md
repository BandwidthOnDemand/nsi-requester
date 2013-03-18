# NSI requester

Start the application by `play run` or `sbt run`.  
You need either Play! or sbt installed. Both available through [Homebrew][homebrew].

    brew install play
    git clone https://github.com/BandwidthOnDemand/nsi-requester.git
    cd nsi-requester
    play run

Move your browser to [http://localhost:9000](http://localhost:9000).

## Running the app on Heroku
[Heroku][heroku] is a cloud application platform that can be used to deploy a Play! application.

### Using the SURFnet instance
You can only do this if you got the privileges on [Heroku][heroku] for this app.

After you cloned this repository you need to run `heroku git:remote --app nsi-requester` to add the remote heroku git repo.  
The application on Heroku is configured by some env variables. You can list them by running `heroku config`. The vars override the properties in `application.properties` by setting them as system properties in the `Procfile`.

Publishing a new version of the app is done by pushing to the heroku remote `git push heroku master`.

### Create your own app on Heroku
After you have cloned the repo.

    heroku create --stack cedar
    git push heroku master

## Eclipse
For editing in Eclipse the [Scala IDE](http://scala-ide.org/) is very usefull.

Eclipse Juno users please use the follwoing update site:
[Eclipse IDE 2.1 milestones](http://download.scala-ide.org/releases-juno-29/milestone/site)

Generating the Eclipse project files can be done by running `eclipse with-source=true` in the sbt console.


[heroku]: http://www.heroku.com
[homebrew]: http://mxcl.github.com/homebrew
