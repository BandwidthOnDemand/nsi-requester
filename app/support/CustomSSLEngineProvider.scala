package support

import javax.net.ssl._
import play.core.ApplicationProvider
import play.server.api._

class CustomSSLEngineProvider(appProvider: ApplicationProvider) extends SSLEngineProvider {
  override def createSSLEngine(): SSLEngine = {
    val sslEngine = SSLContext.getDefault.createSSLEngine
    sslEngine.setNeedClientAuth(true)
    sslEngine
  }
}
