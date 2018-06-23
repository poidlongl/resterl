package net.dietmaier.groovy.rest.utils

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException
import java.security.SecureRandom
import java.security.cert.X509Certificate

// some ssl-voodoo, mainly borrowed from https://gist.github.com/aembleton/889392
class SSLIgnore {
  static ignoreAll() {
    def sc = SSLContext.getInstance("TLS");

    sc.init(
        null,
        [
            new X509TrustManager() {
              X509Certificate[] getAcceptedIssuers() { return null }

              @Override
              void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

              @Override
              void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            }
        ] as TrustManager[],
        new SecureRandom()
    )
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())

    HttpsURLConnection.setDefaultHostnameVerifier(
        new HostnameVerifier() {
          @Override
          boolean verify(String s, SSLSession sslSession) {
            return true
          }
      }
    )
  }
}
