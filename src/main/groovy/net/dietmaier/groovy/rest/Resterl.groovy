package net.dietmaier.groovy.rest

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.AutoClone
import groovy.util.logging.Log4j2

import java.util.regex.Pattern

/**
 * very simple pure-groovy - Implementation of a REST-Client.
 * Design goal was a simple interface that can be used in a script-like context with minimal dependencies to
 * third-party-libs - ok, there's log4j2, simply can't do without :-)
 *
 * Thinks like Thread-Safety, Performance, Throughput and Resource-Consumptions were actively ignored.
 *
 */

@Log4j2
@AutoClone
class Resterl  {
  String url
  String prefix
  String path
  Map query
  def body

  // default values for request properties - may be modified before calling
  def requestProperties = ['Content-Type': 'application/json; charset=utf-8']
  static def responseHandlers = [
      new ResponseHandler([
          name:     'JSON',
          pattern:  ~/.*json.*/,
          handle:  {InputStream is -> new JsonSlurper().parse(is)
          }]),
      new ResponseHandler([
          name:     'XML',
          pattern:  ~/.*xml.*/,
          handle:  {InputStream is -> new JsonSlurper().parse(is)
          }]),
      new ResponseHandler([
          name:     'DEFAULT (Text)',
          pattern:  ~/.*/,
          handle:  {InputStream is -> is.text()
          }]),
  ]

  ResponseHandler getResponseHandler() {
    def r = responseHandlers.find { ResponseHandler h -> h.pattern.matcher(contentType).matches() }
    log.debug("Using response Handler: ${r.name}")
    r
  }

  def last = new Response()

  static String encode(String old) {
    java.net.URLEncoder.encode(old, 'UTF-8')
  }

  static String decode(String old) {
    java.net.URLDecoder.decode(old, 'UTF-8')
  }

  // helper object containing some info about the last response
  // savely adds path tokens to url
  private appendPath(String base, String ext) {
    assert base?: "Base must not be empty"

    def r = base - ~/\/+$/
    if ( ! ext )
      return r
    def ptoken = ext.tokenize('/').collect { encode(it.toString()) }
    if (!ptoken) { // no additional path tokens given
      return r
    }
    return "$r/${ptoken.join('/')}"
  }

  // build url from url, prefix, path and query
  private String buildUrl(String path, Map query) {
    def ret = appendPath(appendPath(url, prefix), path)

    if (query) {
      def querystring = query.collect { name, val -> "$name${ val ? '='+ encode(val.toString()):''}" }.join('&')
      ret = "$ret?$querystring"
    }
    ret
  }


  private HttpURLConnection prepareCon(method) {
    def full = buildUrl(path, query)
    println full
    HttpURLConnection con = new URL(full).openConnection()
    con.with {
      doOutput = true
      doInput = true
      requestMethod = method

//      if ( last.headers.'ETag') {
//        this.requestProperties['If-Match'] = last.headers.'ETag'
//      }
//
      this.requestProperties.each { k, v ->
        addRequestProperty(k, v)
      }
    }
    con
  }

  private def processResponse(HttpURLConnection con) {
    // Todo: Body mabe other format than json
    last = new Response()

    if (body) {
      try {
        con.outputStream << JsonOutput.toJson(body)
      } catch (all) {
        log.warn("Exception in request processing $all")
        last.throwable = all
      }
    }

    processHeaders(con)


    try {
      if (contentLength > 0 ) {
        return responseHandler.handle (
            (last.code >= HttpURLConnection.HTTP_OK && last.code <= HttpURLConnection.HTTP_BAD_REQUEST ) ?
                con.inputStream :
                con.errorStream
        )
      }

    } catch (all) {
      log.warn("Exception in response processing",all)
      last.throwable = all
    }
    return [:]
  }


  private void processHeaders(HttpURLConnection con) {
    last.code = con.responseCode
    last.headers = con.headerFields.collectEntries { String key, Collection vals ->
      switch (vals.size()) {
        case 0: return [(key), '']
        case 1: return [(key), vals.first()]
        default: return [(key), vals]
      }
    }
  }

  def call(String method) {
    def r = processResponse(
        prepareCon(method)
    )
    query = [:]
    body = []
    path = ''
    last.data = r
  }

  // public api from here
  Resterl basicAuth(String user, String password) {
    String auth = "$user:$password".bytes.encodeBase64().toString()
    requestProperties['Authorization'] = "Basic $auth"
    this
  }

  void addResponseHandler(String name, Pattern pattern, Closure handler, int pos = 0 ) {
    responseHandlers.add(pos, new ResponseHandler([name: name, pattern: pattern, handle: handler]))
  }

  int getContentLength() {
    def cl = last.headers.'Content-Length'
    return cl ? cl.toInteger() : -1
  }

  String getContentType() {
    last.headers.'Content-Type'
  }

  Resterl url(String u ) { url = u; this }
  Resterl prefix(String p) { prefix = p; this }
  Resterl path(String p ) { path = p; this }
  Resterl query( Map q ) { query = q; this }
  Resterl body ( b ) { body = b; this }

  Resterl sub(String p) {
    this.clone().prefix(appendPath(prefix, p))
  }

  def get() { call('GET') }
  def post() { call('POST')}
  def put() { call('PUT')}
  def delete() { call ( 'DELETE')}
  def head() { call ( 'HEAD')}
  def options() { call ( 'OPTIONS')}
}

