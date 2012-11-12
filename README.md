# NSI requester

Start the application by `play run` or `sbt run`.  
You need either Play! or sbt installed. Both available through Homebrew.

    brew install play

## Eclipse
For editing in Eclipse the [Scala IDE](http://scala-ide.org/) is very usefull.

Eclipse Juno users please use the follwoing update site:
[Scala 2.9 Nigthly] (http://download.scala-ide.org/nightly-update-juno-master-29x/)

Generating the Eclipse project files can be done by running `eclipse with-source=true` in the sbt console.

## Running the app on Heroku

After you cloned this repository you need to run `heroku git:remote --app nsi-requester` to add the remote heroku repo.
You can only do this if you got the privileges on Heroku.
