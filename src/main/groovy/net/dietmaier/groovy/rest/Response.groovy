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

  void dumpparams() {
    log.info "Response Code: $code"
    headers.each { name, val ->
      log.info "Response Header $name = $val"
    }
    if (throwable) {
      log.warn("Exception thrown:", throwable)
    }
  }
}