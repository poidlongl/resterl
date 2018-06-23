package net.dietmaier.groovy.rest

import groovy.util.logging.Log4j2

/** small utility class to wrap response data of http call
 *
 */
@Log4j2
class Response {
  int code = 0
  Map headers = [:]
  Throwable throwable
  def data
  def method
  def uri

  void dumpparams() {
    log.info "Response Code: $code"
    headers.each { name, val ->
      log.info "Response Header $name = $val"
    }
    if (throwable) {
      log.warn("Exception thrown:", throwable)
    }
  }

  // helper functions
  int getContentLength() {
    def cl = headers.'Content-Length'
    return cl ? cl.toInteger() : -1
  }

  String getContentType() {
    headers.'Content-Type'
  }

  void processResponse(HttpURLConnection con) {
    code = con.responseCode
    headers = con.headerFields.collectEntries { String key, Collection vals ->
      switch (vals.size()) {
        case 0: return [(key), '']
        case 1: return [(key), vals.first()]
        default: return [(key), vals]
      }
    }
  }

  boolean isValid() {
    code >= HttpURLConnection.HTTP_OK && code <= HttpURLConnection.HTTP_BAD_REQUEST
  }

  boolean isNotFound() {
    return code == HttpURLConnection.HTTP_NOT_FOUND
  }
}