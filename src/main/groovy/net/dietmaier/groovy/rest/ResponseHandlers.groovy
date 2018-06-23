package net.dietmaier.groovy.rest

class ResponseHandlers {
  static RETHROW = { con, Response last ->
    if ( last.throwable ) {
      throw new Exception("Failed calling ${last.method} on ${last.uri}", last.throwable)
    }
    last.data
  }

  static FAILONSTATUS = { con, Response last ->
    if ( ! last.valid ) {
      throw new Exception("${last.method} on ${last.uri} failed with code ${last.code}")
    }
  }
}
