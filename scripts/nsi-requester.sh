#!/bin/bash

#############################################################################
# Example bash script for the nsi-requester
#
# This script provides an example of how commandline options can be used to
# configure the nsi-requester process.
#
#############################################################################

# The user and user group the application will run under.  It is a good idea
# to have this as a restricted user.
USER=hacksaw
GROUP=hacksaw

# The IP address and port the application will open for end user access.  If
# you are fronting the application with Apache httpd and mod_proxy then this
# can be restricted to an internal port.
ADDRESS="127.0.0.1"
PORT="9001"
HTTPS_PORT="9443"

# Install location for the nsi-requester.
HOME=/Users/hacksaw/src/github/nsi-requester/target/universal/stage

# Custom configuration and log files that will override those compiled into
# the application at build time.
CONFIG=$HOME/conf/application.conf
LOG=$HOME/conf/application-logger.xml

# This application was designed to be fronted (mod_ssl) and backed (stunnel)
# by separate processes supporting SSL.  If you want to remove the need for
# these components then manage certificates through the Java Key and Trust
# stores.
KEYSTORE=$HOME/conf/keystore.jks
TRUSTSTORE=$HOME/conf/truststore.jks
PASSWORD="changeit"

# Configure your specific JVM runtime needs here.
EXTRA="-J-Xms512m -J-Xmx512m -J-server -J-verbose:gc
-J-XX:+PrintGCDetails -J-Xloggc:/var/log/nsi-requester/gc.log
-J-XX:+UseGCLogFileRotation -J-XX:NumberOfGCLogFiles=10
-J-XX:GCLogFileSize=10M -J-XX:+UseParallelGC -J-XX:+UseParallelOldGC"

# Start the nsi-requester under the specified user.
exec su -l -s /bin/bash -c 'exec "$0" "$@"' $USER -- \
        $HOME/bin/nsi-requester \
        -Dconfig.file=$CONFIG \
        -Dlogger.file=$LOG \
        -Dhttp.address=$ADDRESS \
	-Dhttps.port=$HTTPS_PORT \
        -Djavax.net.ssl.keyStore=$KEYSTORE \
        -Djavax.net.ssl.keyStorePassword=$PASSWORD \
        -Djavax.net.ssl.trustStore=$TRUSTSTORE \
        -Djavax.net.ssl.trustStorePassword=$PASSWORD \
        -DapplyEvolutions.default=true $EXTRA

