package examples

import net.dietmaier.groovy.rest.Resterl

// Base-URL, prefix and basicAuth will be kept in the instance
Resterl things = new Resterl([
          url: 'http://localhost:8080',
          prefix: 'things'
        ]).basicAuth('admin', 'changeit')

// create the database
things.query([
    desc: 'a database with very important things'
]).put()

// check response
assert [
  200, // returned if DB already exists
  201  // returned if DB is newly created
].contains(things.last.code) : "DB-Creation failed"

// create first collection
def colored = things.sub('colored')

// get status of colored
colored.get()
// delete it of it exists
if ( colored.last.valid ) {
  RestheartHelper.setMatch(colored)
  colored.delete() // delete if exists
}

//recreate collection
colored.query([
    desc: 'a collection for colored things'
]).put()

// add some things
colored.body([
    name: 'banana',
    color: 'yellow'
]).post()

colored.body([
    [ name: 'rose', color: 'red'],
    [ name: 'peacock', color: 'blue'],
    [ name: 'yellow brick road', color: 'pink' ]
]).post()

// correct a mistake
def ybr = colored.get()._embedded.find { it.name == 'yellow brick road'}

RestheartHelper.setMatch(colored, ybr)
def id = RestheartHelper.idOf(ybr)
ybr.color = 'yellow'

// upsert document - update would need PATCH - Support
colored.path(id).body(
    RestheartHelper.sanitized(ybr)
).put()

// a look at the result
colored.get()._embedded.each {
  println RestheartHelper.sanitized(it)
}
