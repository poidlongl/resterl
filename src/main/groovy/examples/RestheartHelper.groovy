package examples

import net.dietmaier.groovy.rest.Resterl

class RestheartHelper {
  // set If-Match - Header from last response
  static void setMatch(Resterl thing) {
    def etag = thing.last.headers.ETag

    if (etag)
      thing.requestProperties.'If-Match' = etag
  }

  // set If-Match - Header from single Object
  static void setMatch(Resterl thing, Map values) {
    def etag = etagOf(values)
    if (etag)
      thing.requestProperties.'If-Match' = etag
  }

  // Extract document-ID from single doc
  static String idOf(Map object ) {
    object?._id?.'$oid'
  }

  // extract etag from object
  static String etagOf(Map object ) {
    object?._etag?.'$oid'
  }

  static sanitized(Map object) {
    object.findAll { ! ['_id', '_etag'].contains(it.key)}
  }
}
