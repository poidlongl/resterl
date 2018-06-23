package examples

import net.dietmaier.groovy.rest.Resterl

class Example {
  static Resterl resterl = new Resterl([url: 'http://localhost:8080', prefix: 'firstdb' ])

  static void main(String[] args){
    resterl.basicAuth('admin', 'changeit')
    resterl.path('mycol').put()
    resterl.last.dumpparams()
    resterl.path('mycol').body(['k1': 'value 1']).put()
    resterl.last.dumpparams()
    def result = resterl.path('mycol').query(['pagesize':100, np: null ])get()
    resterl.last.dumpparams()
    println result
  }
}
