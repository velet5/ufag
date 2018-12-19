import client.{HttpClientImpl, Request, Response, RestClientImpl}
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClientBuilder}
import org.apache.http.ssl.SSLContextBuilder

trait Clients {

  /** Java complains about Lingvo.com SSL certificate */
  private val sslContext =
    SSLContextBuilder.create()
      .loadTrustMaterial(new TrustSelfSignedStrategy())
      .build()

  private val config: RequestConfig = RequestConfig.custom().setConnectTimeout(500).build()
  private val httpAsyncClient: CloseableHttpAsyncClient =
    HttpAsyncClientBuilder.create()
      .setDefaultRequestConfig(config)
      .setSSLContext(sslContext)
      .build()

  lazy val restClient = new RestClientImpl(
    new HttpClientImpl[Request, Response](httpAsyncClient)
  )

}
