package net.dietmaier.groovy.rest.utils

class Config {
  static defaults = [
      'ignore.sslissues': true
  ]

  static String getSetting(String key, Map context = Collections.EMPTY_MAP) {
    context[key] ?:
    System.getenv(key.toUpperCase().replaceAll('.', '_')) ?:
    System.properties[key] ?:
    defaults[key]
  }
}
