package examples

import net.dietmaier.groovy.rest.Resterl

void handleETag(Resterl thing ) {
  def etag = thing.last.headers.ETag

  if ( etag )
    thing.requestProperties.'If-Match' = etag
}

// Base-URL, prefix and basicAuth will be kept in the instance
Resterl things = new Resterl([
          url: 'http://localhost:8080',
          prefix: 'things'
        ]).basicAuth('admin', 'changeit')

// create the database
things.query([
    desc: 'a database with very important things'
]).put()

assert [
  200, // returned if DB already exists
  201  // returned if DB is newly created
].contains(things.last.code) : "DB-Creation failed"

// create first collection
def colored = things.sub('colored')

def curr = colored.get()
handleETag(colored)
colored.delete() // delete if exists
println "delete collection: ${colored.last.code}"

colored.query([
    desc: 'a collection for colored things'
]).put()

// add some things

colored.body([
    name: 'banana',
    color: 'yellow'
]).post()

println colored.get()._embedded

colored.body([
    [ name: 'rose', color: 'red'],
    [ name: 'peacock', color: 'blue'],
    [ name: 'yellow brick road', color: 'pink' ]
]).post()

def current = colored.get()._embedded

current.each {
  println it
}

// correct a mistake
def ybr = current.find{it.name == 'yellow brick road'}
colored.requestProperties.'If-Match' = ybr._etag.'$oid'

def id = ybr._id.'$oid'
ybr.color = 'yellow'
ybr.remove('_etag')
ybr.remove('_id')

println "putting: $ybr"

colored.path(id).body(
    ybr
).put()

colored.last.dumpparams()

current = colored.get()

current._embedded.each {
  println it
}
