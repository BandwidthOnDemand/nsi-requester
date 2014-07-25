package support

import javax.net.ssl._
import play.core.ApplicationProvider
import play.server.api._

/*
 * This custom SSL engine creates an instance of the default SSL Engine and
 * enables client authentication.  For this class to be instantiated the
 * following command line argument must be used when starting the application:
 *
 *     -Dplay.http.sslengineprovider=support.CustomSSLEngineProvider
 */
class CustomSSLEngineProvider(appProvider: ApplicationProvider) extends SSLEngineProvider {
  override def createSSLEngine(): SSLEngine = {
    val sslEngine = SSLContext.getDefault.createSSLEngine
    sslEngine.setNeedClientAuth(true)
    sslEngine
  }
}
