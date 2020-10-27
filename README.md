# scala-akka-http-actor-workshop
* https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f

* This leads to two goals: complexity has to stay as low as possible, and resources
  must be used efficiently while you scale the application.
* The Reactive Manifesto
    * Blocking I/O limits opportunities for parallelism, so nonblocking I/O is preferred.
    * Synchronous interaction limits opportunities for parallelism, so asynchronous interaction is preferred.
    * Polling reduces opportunity to use fewer resources, so an event-driven style is preferred.
    * If one node can bring down all other nodes, that’s a waste of resources. So
    you need isolation of errors (resilience) to avoid losing all your work.
    * Systems need to be elastic: If there’s less demand, you want to use fewer resources. 
        * If there’s more demand, use more resources, but never more than required.
