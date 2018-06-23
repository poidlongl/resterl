package net.dietmaier.groovy.rest

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.AutoClone
import groovy.util.logging.Log4j2
import net.dietmaier.groovy.rest.utils.Config
import net.dietmaier.groovy.rest.utils.SSLIgnore

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
  static {
    if ( Config.getSetting('ignore.sslissues')) {
      SSLIgnore.ignoreAll()
    }
  }

  String url
  String prefix
  String path
  Map query
  def body

  // default values for request properties - may be modified before calling
  def requestProperties = ['Content-Type': 'application/json; charset=utf-8']

  // default content handlers - may be extended by adding new Instancef of ContentHandler to contentHandlers-Collection
  static contentHandlers = [
      new ContentHandler([
          name:     'JSON',
          pattern:  ~/.*json.*/,
          handle:  {InputStream is -> new JsonSlurper().parse(is)
          }]),
      new ContentHandler([
          name:     'XML',
          pattern:  ~/.*xml.*/,
          handle:  {InputStream is -> new JsonSlurper().parse(is)
          }]),
      new ContentHandler([
          name:     'DEFAULT (Text)',
          pattern:  ~/.*/,
          handle:  {InputStream is -> is.text
          }]),
  ]

  static responseFilters = [
      ResponseFilter.RETHROW
  ]

  // get response handler according to contentType
  ContentHandler getContentHandler() {
    def r = contentHandlers.find { ContentHandler h -> h.pattern.matcher(last.contentType).matches() }
    log.debug("Using response Handler: ${r.name}")
    r
  }

  // keeps status info of last request
  def last = new Response()

  // some helper functions

  // URL-Encode String
  static String encode(String old) {
    URLEncoder.encode(old, 'UTF-8')
  }

  // URL-Decode String
  static String decode(String old) {
    URLDecoder.decode(old, 'UTF-8')
  }

  // savely adds path tokens
  private static appendPath(String base, String ext) {
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


  // setup
  private HttpURLConnection prepareCon(method) {
    def full = buildUrl(path, query)
    log.info("calling method $method on $full")
    last = new Response([method: method, uri: full ])
    HttpURLConnection con = (HttpURLConnection) new URL(full).openConnection()
    con.with {
      doOutput = true
      doInput = true
      requestMethod = method
      this.requestProperties.each { k, v ->
        addRequestProperty(k, v)
      }
    }
    con
  }

  private def processResponse(HttpURLConnection con) {
    // should do some thinking about this part, may be problematic in some cases
    def response = [:]
    if (body) {
      try {
        con.outputStream << JsonOutput.toJson(body)
      } catch (all) {
        log.warn("Exception in request processing $all")
        last.throwable = all
      }
    }

    last.processResponse(con)

    try {
      if (last.contentLength > 0 ) {
        response =  contentHandler.handle (
            ( last.valid ) ?
                con.inputStream :
                con.errorStream
        )
      }

    } catch (all) {
      log.warn("Exception in response processing",all)
      last.throwable = all
    }

    responseFilters.each { it(con, last) }
    response
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
    requestProperties.Authorization = "Basic $auth"
    this
  }

  // add new response handler
  static void addResponseHandler(String name, Pattern pattern, Closure handler, int pos = 0 ) {
    contentHandlers.add(pos, new ContentHandler([name: name, pattern: pattern, handle: handler]))
  }

  // builder functions
  Resterl url(String u ) { url = u; this }
  Resterl prefix(String p) { prefix = p; this }
  Resterl path(String p ) { path = p; this }
  Resterl query( Map q ) { query = q; this }
  Resterl body ( b ) { body = b; this }

  // creates a new instance with extended prefix
  Resterl sub(String p) {
    this.clone().prefix(appendPath(prefix, p))
  }
  // returns parent instance
  Resterl parent() {
    this.clone().prefix(prefix.tokenize('/').dropRight(1).join('/'))
  }

  // operations
  def get() { call('GET') }
  def post() { call('POST')}
  def put() { call('PUT')}
  def delete() { call ( 'DELETE')}
  def head() { call ( 'HEAD')}
  def options() { call ( 'OPTIONS')}
}
