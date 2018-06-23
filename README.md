# resterl
HTTPUrlConnection - based Groovy-Lib to handle REST-Like APIs 

Resterl is a simple wrapper around HTTPUrlConnection intented to be used in Script-Like Usecases. 

It's   
 - small and handy for simple tasks
 - pure groovy without further dependencies ( well, with the exception of log4j2 - but: java-logging? really? will never get used to it )
 - optimized for some every-day tasks in my environment - see Examples below
 - NOT a fullfeatured all-purpose http-client
 - NOT designed and tested for long-running services, multithreaded, high-performance, high-throughput or other sophisticated requirements
 - NOT following any specific design pattern, allthough it borrows some ideas from the one or the other
 
a local docker-based installation of https://restheart.org/ is used for testing + examples
  
##Installation

Currently: clone from here, build a jar and put it on your classpath ( + the log4j2 - deps - see build.gradle )
Will be improved + documented somewhen in the future ( allthough i've currently no idea how and especially when )
 
## Some Examples


```groovy
// Create a new Database - see https://restheart.org/learn/tutorial/#create-a-database

import net.dietmaier.groovy.rest.Resterl

def things = new Resterl('https://localhost:8080', 'things').basicAuth(
      'admin', 'changeig'
    ).query([
      description: 'some very important things'
    ]).put()    
// note that the baseurl, auth and the prefix are remembered in the Resterl-instance

// now, we create an     
 
```

