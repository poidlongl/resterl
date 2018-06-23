package net.dietmaier.groovy.rest

import spock.lang.Shared
import spock.lang.Specification

class ResterlTest extends Specification {

  @Shared db = initdb()

  def initdb() {
    new Resterl(url: 'http://localhost:8080', prefix: 'testing').
        basicAuth('admin','changeit')
  }

  def "createDb"() {
    when:
      db.put()
    then:
      [ 200, 201 ].contains( db.last.code )
  }

  def "createCollection"() {
    when:
      def collection = db.sub('colname')
      collection.put()
    then:
      collection.last.valid

  }

  def "createDocument"() {
    when:
      db.path('colname').body( [ name: 'fred', profession: 'bronto crane operator' ] ).post()
    then:
      db.last.valid
  }

  def "createDocuments"() {
    when:
    db.path('colname').body( [
        [ name: 'barney', profession: 'top secret' ],
        [ name: 'ed', profession: 'handyman']
    ]).post()
    then:
    db.last.valid
  }

  def "deleteCollection"() {
    when:
      def col = db.get()._embedded.find {it._id == 'colname'}
      if ( col ) {
        db.requestProperties.'If-Match' = col._etag.'$oid'
        db.path('colname').delete()
      }
    then:
      db.last.valid
  }

  def "deleteDB"() {
    when:
      db.get()
      db.requestProperties.'If-Match' = db.last.headers.ETag
      db.delete()
    then:
      db.last.valid
  }
}
