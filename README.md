[![Build Status](https://app.travis-ci.com/mtumilowicz/scala-akka-http-workshop.svg?branch=master)](https://travis-ci.com/mtumilowicz/scala-akka-http-actor-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# scala-akka-http-workshop
* references
    * https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f
    * https://www.manning.com/books/akka-in-action
    * https://doc.akka.io/docs/akka-http/10.2.1/

## preface
* goals of this workshop
    * introduction to akka http
        * routing and marshalling
* workshop plan:
    * playground with roles: developer, product owner
    * develop microservice with features
        * user could buy a venue if he can afford it
        * userA could buy a venue from userB if he can afford it
          
## akka http
* implements a full server/client-side HTTP stack on top of akka-actor and akka-stream
* provides a DSL to describe HTTP "routes" and how they should be handled
    * Route is the central concept of Akka HTTP’s Routing DSL
        ```
        type Route = RequestContext => Future[RouteResult]
        ```
        * when a route receives a request (RequestContext) it can do one of these things
            1. requestContext.complete(...) - given response is sent to the client as reaction to the request
            1. requestContext.reject(...) - route does not want to handle the request
            1. requestContext.fail(...) - 
        * RequestContext
            * wraps an HttpRequest instance to enrich it with additional information that are typically required 
            by the routing logic (ex. ExecutionContext)
        * RouteResult
            * simple abstract data type (ADT) that models the possible non-error results of a Route
            * Complete, Rejected
    * Directive
        * a small building block used for creating route structures
            ```
            val route: Route = { ctx => ctx.complete("yeah") } // standard way to build route
            val route: Route = _.complete("yeah") // scala syntax
            val route = complete("yeah") // complete directive
            ```
        * example
            ```
            val route: Route =
            pathPrefix("venues") {
                concat(
                    pathEnd {
                        get {
                            complete(handler.action)
                        }
                    },
                  ...
                )
                }
            ```
    * PathMatcher
        * mini-DSL is used to match incoming URL’s and extract values from them
        * used in the path directive
        * example
            ```
            path("foo" / "bar") // matches /foo/bar
            ```
* marshalling and unmarshalling
    * marshalling - converting a higher-level (object) into lower-level representation, ex. a "wire format"
        * also called "serialization" or "pickling"
    * "unmarshalling" - reverse process to marshalling
        * also called "deserialization" or "unpickling"
    * is done separately from the route declarations
        * marshallers are pulled in implicitly using the "magnet" pattern
            * you can use any kind of object as long as there is an implicit marshaller available in scope
            * http://spray.io/blog/2012-12-13-the-magnet-pattern/ 
* timeouts
    * idle-timeout - if a connection is open but no request/response is being written to it for over idle-timeout time, 
    the connection will be automatically closed.
    * request timeouts - limits the maximum time it may take to produce an HttpResponse from a route
        * if that deadline is not met the server will automatically inject a Service Unavailable HTTP response and 
        close the connection to prevent it from leaking and staying around indefinitely
