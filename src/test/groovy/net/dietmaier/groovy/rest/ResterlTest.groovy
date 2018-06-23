package net.dietmaier.groovy.rest

import spock.lang.Shared
import spock.lang.Specification

class ResterlTest extends Specification {

  @Shared client = initclient()

  def initclient () {
    def cl = new Resterl(url: 'http://localhost:8080', prefix: 'testing')
    cl.basicAuth('admin','changeit')
    cl
  }

  def "Put"() {
    when:
      client.put()
    then:
      [ 200, 201 ].contains( client.last.code )
  }

  def "Get"() {
    when:
      def result = client.get()
      println result
      client.last.dumpparams()
    then:
      1==1

  }

  def "Post"() {
    when:
      client.put('mycol', [:], ['unpatched': 'yes'])
      def result = client.get('mycol')
    then:
      result.unpatched == 'yes'
  }

  def "Patch"() {
    // patch is not supported by HTTPUrlConnection, has to be workarounded ...
/*    when:
      client.patch('mycol',[:], ['patched': 'yes'])
      client.last.dumpparams()
      def result =  client.get('mycol')
    then:
      result.patched == 'yes'
*/
  }

  def "Delete"() {
  }
}
