#!/bin/bash

#############################################################################
# Example bash script for the nsi-requester
#
# This script provides an example of how command line options can be used to
# configure the nsi-requester process.  This example is for a deployment
# where native SSL/TLS support is used for all communications.
#
#############################################################################

# The IP address and port the application will open for end user access.  If
# you are fronting the application with Apache httpd and mod_proxy then this
# can be restricted to an internal port.  If an address is not provided then
# the play framework will bind to all addresses.
ADDRESS="127.0.0.1"
HTTP_PORT="9001"

# Install location for the nsi-requester (i.e. wherever the contents of the
# target/universal/stage directory were place).
HOME=.

# Custom configuration and log files that will override or extend those
# compiled into the application at build time.
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

# Start the nsi-requester.
$HOME/bin/nsi-requester \
        -Dconfig.file=$CONFIG \
        -Dlogger.file=$LOG \
        -Dhttp.address=$ADDRESS \
        -Dhttp.port=$HTTP_PORT \
        -Djavax.net.ssl.keyStore=$KEYSTORE \
        -Djavax.net.ssl.keyStorePassword=$PASSWORD \
        -Djavax.net.ssl.trustStore=$TRUSTSTORE \
        -Djavax.net.ssl.trustStorePassword=$PASSWORD \
        -DapplyEvolutions.default=true $EXTRA

# Add command line option -Djavax.net.debug="all" if you need to debug SSL.
