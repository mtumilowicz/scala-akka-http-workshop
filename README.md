# scala-akka-http-actor-workshop
* https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f
* https://www.manning.com/books/akka-in-action
* https://doc.akka.io/docs/akka-http/10.2.1/

## preface
* https://github.com/mtumilowicz/scala213-functional-programming-collections-workshop (scala intro)
* https://github.com/mtumilowicz/kotlin-functional-programming-actors-workshop (actors intro)
    
## introduction
* two ultimate goals during software development
    1. complexity has to stay as low as possible
    1. resources must be used efficiently while you scale the application
* if we want to scale, the programming model has to be asynchronous
    * allows components to continue working while others haven’t responded yet
    * actor model chooses the abstraction of sending and receiving messages
* actors vs synchronous approach
    |                                   |actors   |synchronous approach   |
    |---                                |---      |---|
    |scaling                            |send and receive messages, no shared mutable state, immutable log of events    |mix of threads, shared mutable state in a CRUD database, and web service RPC calls   |
    |providing interactive information  |event-driven: push when the event occurs                                       |poll for current information    |
    |scaling out on the network         |asynchronous messaging, nonblocking I/O                                        |synchronous RPC, blocking I/O       |
    |handling failures                  |let it crash, isolate failure, and continue without failing parts              |handle all exceptions; only continue if everything works   |
* actors: decoupled on three axes
    * space/location
        * actor gives no guarantee and has no expectation about where another actor is located
    * time
        * actor gives no guarantee and has no expectation about when its work will be done
    * interface
        * nothing is shared between actors
        * information is passed in messages
    * system built out of components coupled on all three axes can only exist on
      one runtime and will fail completely if one of its components fails
        
## constructs
* ActorSystem
    * is a hierarchical group of actors which share common configuration, e.g. dispatchers, deployments, 
    remote capabilities and addresses
    * entry point for creating or looking up actors.
    * actors can create other actors, but who creates the first one?
        * first thing that every Akka application does is create an ActorSystem
    * You could compare the hierarchy of actors to a URL path structure.
        * ActorPath
        * Every actor has a name. 
        * This name needs to be unique per level in the hierarchy: two sibling actors can’t have the same 
        name
        * if you don’t provide a name, Akka generates one for you, but it’s a good idea to name all your actors
        * All actor references can be located directly by an actor path, absolute or relative.
* ActorRef
    * immutable and serializable handle to an actor
    * ActorSystem returns an address (ActorRef) to the created top-level actor instead of the actor itself
    * can be used to send messages to the actor
    * makes sense - actor could be on another server
    * overview: ActorRef -> Mailbox -> Actor
* Dispatchers
    * In the real world, dispatchers are the communication coordinators responsible for receiving and passing 
    messages. 
        * For example, with emergency services like 911, the dispatchers are the people responsible for taking 
        in the call and passing on the messages to the other departments like the medical, fire station, 
        police, etc. 
    * are said to be the main engine of an ActorSystem
    * are responsible for selecting an actor and it’s messages and assigning them the CPU
        * actors are lightweight because they run on top of dispatchers
            * the actors aren’t necessarily directly proportional to the number of threads
            * actors take a lot less space than threads: around 2.7 million actors can fit in 1 GB of memory. 
            * That’s a big difference compared to 4096 threads for 1 GB of memory
    * Every ActorSystem will have a default dispatcher that will be used in case nothing else is configured 
    * actors are invoked at some point by a dispatcher
        * dispatcher pushes the messages in the mailbox through the actors
    * context.spawn(yourBehavior, "DispatcherFromConfig", DispatcherSelector.fromConfig("your-dispatcher"))
        * custom dispatcher from configuration and relies on this being in your application.conf:
            ```
            my-dispatcher {
                # Dispatcher is the name of the event-based dispatcher
                type = Dispatcher
                # What kind of ExecutionService to use
                executor = "fork-join-executor"
                # Configuration for the fork join pool
                fork-join-executor {
                    # Min number of threads to cap factor-based parallelism number to
                    parallelism-min = 2
                    # Parallelism (threads) ... ceil(available processors * factor)
                    parallelism-factor = 2.0
                    # Max number of threads to cap factor-based parallelism number to
                    parallelism-max = 10
                }
                # Throughput defines the maximum number of messages to be
                # processed per actor before the thread jumps to the next actor.
                # Set to 1 for as fair as possible.
                throughput = 100
            }
            ```
* An Actor is given by the combination of a Behavior and a context in which this behavior is executed
    * ActorContext
        * An ActorContext in addition provides access to the Actor’s own identity ("self"), the ActorSystem 
        it is part of, methods for querying the list of child Actors it created, access to Terminated and timed 
        message scheduling.
        * context.spawn() creates a child actor
        * system.spawn() creates top level
    * Behavior
        * The behavior of an actor defines how it reacts to the messages that it receives
        ```
        def apply(): Behavior[SayHello] =
            Behaviors.setup { context => // typically used as the outer most behavior when spawning an actor
                val greeter = context.spawn(HelloWorld(), "greeter")
            
                Behaviors.receiveMessage { message => // useful for when the context is already accessible by other means, like being wrapped in an [[setup]] or similar
                    val replyTo = context.spawn(HelloWorldBot(max = 3), message.name)
                    greeter ! HelloWorld.Greet(message.name, replyTo)
                    Behaviors.same
                }
            }
        ```
        
# failure
* actor provides two separate flows
    * one for normal logic
        * consists of actors that handle normal messages
    * one for fault recovery logic
        * consists of actors that monitor the actors in the normal flow
* instead of catching exceptions in an actor, we’ll just let the actor crash
* The actor code for handling messages only contains normal processing logic and no error han-
  dling or fault recovery logic, so it’s effectively not part of the recovery process, which
  keeps things much clearer
* The mailbox for a crashed actor is suspended until the
  supervisor in the recovery flow has decided what to do with the exception
* How does an actor become a supervisor? 
    * Akka has chosen to enforce parental supervision, meaning that any actor that creates 
    actors automatically becomes the supervisor of those actors.
    * A supervisor doesn’t “catch exceptions;” rather it decides what should happen with the
      crashed actors that it supervises based on the cause of the crash
* The supervisor doesn’t try to fix the actor or its state. 
    * It simply renders a judgment on how to recover, and then triggers the corresponding strategy
* The supervisor has four options when deciding what to do with the actor:
    * Restart
        * The actor must be re-created from its Props
        * After it’s restarted (or rebooted, if you will), the actor will continue to process messages. 
        * Since the rest of the application uses an ActorRef to communicate with the actor, the new
        actor instance will automatically get the next messages.
    * Resume
        * The same actor instance should continue to process messages; 
        * the crash is ignored.
    * Stop
        * The actor must be terminated
        * It will no longer take part in processing messages.
    * Escalate
        * The supervisor doesn’t know what to do with it and escalates the problem to its parent, 
        which is also a supervisor.
* in most cases, you don’t want to reprocess a message, because it probably caused the
  error in the first place
  * An example of that would be the case of the logProcessor
    encountering a corrupt file: reprocessing corrupt files could end up in what’s called a
    poisoned mailbox—no other message will ever get processed because the corrupting
    message is failing over and over again
  * For this reason, Akka chooses not to provide
   the failing message to the mailbox again after a restart, but there’s a way to do this
   yourself if you’re absolutely sure that the message didn’t cause the error
# testing
* An actor hides its internal state and doesn’t allow access to this state
    * Calling a method
      on an actor and checking its state, which is something you’d like to be able to
      do when unit testing, is prevented by design

## 4.2 Actor lifecycle
* An actor is automatically started by Akka when it’s created. 
* The actor will stay in the Started state until it’s stopped, at which point the actor is in the Terminated state
* When the actor is terminated, it can’t process messages anymore and will be eventually garbage collected
* When the actor is in a Started state, it can be restarted to reset the internal state of the actor
* During the lifecycle of an actor, there are three types of events:
    * The actor is created and started—for simplicity we’ll refer to this as the start event.
    * The actor is restarted on the restart event.
    * The actor is stopped by the stop event.
* There are several hooks in place in the Actor trait, which are called when the events
  happen to indicate a lifecycle change
  * You can add some custom code in these hooks
    that can be used to re-create a specific state in the fresh actor instance, for example, to
    process the message that failed before the restart, or to clean up some resources
  * The order in which the hooks occur is guaranteed, although they’re
    called asynchronously by Akka.
* 4.2.1 Start event
    * An actor is created and automatically started with the actorOf method
    * Top-level actors are created with the actorOf method on the ActorSystem
    * A parent actor creates a child actor using the actorOf on its ActorContext
    * After the instance is created, the actor will be started by Akka. 
    * The preStart hook is called just before the actor is started.
* 4.2.2 Stop event
    * The stop event indicates the end of the actor
      lifecycle and occurs once, when an actor is
      stopped
    * An actor can be stopped using
      the stop method on the ActorSystem and
      ActorContext objects, or by sending a
      PoisonPill message to an actor
    * The postStop hook is called just before the actor is terminated. 
        * possibly stores the last state of the
          actor somewhere outside of the actor in the case that the next actor instance needs it
    * When the actor is in the Terminated state, the actor doesn’t get any new messages to handle.
    * A stopped actor is disconnected from its ActorRef
    * After the actor is stopped, the
      ActorRef is redirected to the deadLettersActorRef of the actor system, which is a
      special ActorRef that receives all messages that are sent to dead actors.
* 4.2.3 Restart event
    * During the lifecycle of an actor, it’s possible that its supervisor will decide that the
      actor has to be restarted.
    * This event is more complex than the start or stop events,
      because the instance of an actor is replaced
    * When a restart occurs, the preRestart method of the crashed actor instance is called.
        * In this hook, the crashed actor instance is able to store its current state, just before it’s
          replaced by the new actor instance.
    * Be careful when overriding this hook. 
        * The default implementation of the preRestart method stops all the child actors of the actor 
        and then calls the postStop hook.
        * If you forget to call super.preRestart , this default behavior won’t occur
        * If the
          children of the crashed actor aren’t stopped, you could end up with increasingly more
          child actors when the parent actor is restarted.
    * It’s important to note that a restart doesn’t stop the crashed actor in the same way
      as the stop methods
      * A crashed actor instance in a restart doesn’t cause a Terminated message to be sent for the crashed actor
      * The fresh
        actor instance, during restart, is connected to the same ActorRef the crashed actor
        was using before the fault
        * A stopped actor is disconnected from its ActorRef and
          redirected to the deadLettersActorRef as described by the stop event
      * What both
        the stopped actor and the crashed actor have in common is that by default, the post-
        Stop is called after they’ve been cut off from the actor system.
    * The solution in that case could be to
      send the failed Row message to the self ActorRef so it would be processed by the
      fresh actor instance
      * One issue to note with this approach is that by sending a message
        back onto the mailbox, the order of the messages on the mailbox is changed
* 4.2.4 Putting the lifecycle pieces together
* 4.2.5 Monitoring the lifecycle
    * The lifecycle ends when the actor is terminated. 
        * An actor is terminated
            * if the supervisor decides to stop the actor
            * if the stop method is used to stop the actor
            * if a PoisonPill message is sent to the actor, which indirectly causes the stop method to be called.
        * Since the default implementa-
          tion of the preRestart method stops all the actor’s children with the stop methods,
          these children are also terminated in the case of a restart
    * The crashed actor instance in a restart isn’t terminated in this sense
        * This is because the ActorRef will continue to live on after the restart; 
        * the actor instance hasn’t been terminated, but replaced by a new one
    * The ActorContext provides a watch method to monitor the death of an actor and an unwatch to 
    de-register as monitor. 
        * Once an actor calls the watch method on an actor reference, it becomes the monitor of that actor reference.
        * A Terminated message is sent to the monitor actor when the monitored actor is terminated.
        * The Terminated message only contains the ActorRef of the actor that died
    * The fact that the crashed actor instance in a restart isn’t terminated in the same way
      as when an actor is stopped now makes sense, because otherwise you’d receive many
      terminated messages whenever an actor restarts, which would make it impossible to
      differentiate the final death of an actor from a temporary restart
    * As opposed to supervision, which is only possible from parent to child actors, monitor-
      ing can be done by any actor
## 4.3 Supervision
* 4.3.1 Supervisor hierarchy
    * every actor that creates another is the supervisor of the created child actor
    * The supervision hierarchy is fixed for the lifetime of a child actor
        * there’s no such thing as adoption in Akka
    * The most dangerous actors (actors that are most likely to crash) should be as low
      down the hierarchy as possible
      * When a fault occurs in the top level of the actor system, it could restart all the
        top-level actors or even shut down the actor system.
    * If an actor instance were to be
      stopped, the ActorRef would refer to the system’s deadLetters , which would break
      the application
* 4.3.2 Predefined strategies
    * The top-level actors in an application are created under the /user path and supervised
      by the user guardian
    * The default supervision strategy for the user guardian is to restart
      its children on any Exception , except when it receives internal exceptions that indicate
      that the actor was killed or failed during initialization, at which point it will stop the
      actor in question
    * There are two predefined strategies available in the Supervisor-
      Strategy object: the defaultStrategy and the stoppingStrategy
        * The stopping strategy will stop any child that crashes on any Exception 
    * Akka allows you to make a decision about the fate of the child actors in two ways: 
        * all children share the same fate and the same recovery is applied to the lot, 
        * or a decision is rendered and the remedy is applied only to the crashed actor
        * In some cases you might want to stop only the child actor that failed
        * In other cases you might want to stop all child actors if one of them fails, maybe because 
        they all depend on a particular resource
            * If an exception is thrown that indicates that the shared resource has failed
              completely, it might be better to immediately stop all child actors together instead of
              waiting for this to happen individually for every child
    * The OneForOneStrategy deter-
      mines that child actors won’t share the same fate: only the crashed child will be
      decided upon by the Decider 
    * The other option is to use an AllForOneStrategy ,
      which uses the same decision for all child actors even if only one crashed
    * Any Throwable that isn’t handled by the supervisor strategy
      will be escalated to the parent of the supervisor
    * If a fatal error reaches all the way up
      to the user guardian, the user guardian won’t handle it, since the user guardian uses
      the default strategy
      * an uncaught exception handler in the actor system causes the actor system to shut down
      * In most cases it’s good practice not to handle
        fatal errors in supervisors, but instead gracefully shut down the actor system, since a
        fatal error can’t be recovered from
* 4.3.3 Custom strategies
    * there are four different types of actions a supervisor can take to resolve a crashed actor
        * Resume the child, ignore errors, and keep processing with the same actor instance.
        * Restart the child, remove the crashed actor instance, and replace it with a fresh actor instance.
        * Stop the child and terminate the child permanently.
        * Escalate the failure and let the parent actor decide what action needs to be taken.
    * The OneForOneStrategy and AllForOneStrategy will continue indefinitely by
      default. 
      * Both strategies have default values for the constructor arguments maxNrOfRetries and withinTimeRange
      ```
      override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 60 seconds) {
        case _: DbBrokenConnectionException => Restart
      }
      ```
      Escalates the issue if the problem hasn’t been resolved within 60 seconds or it has failed to be 
      solved within five restarts
    * It’s important to note that there’s no delay between restarts; the actor
      will be restarted as fast as possible
        * This BackOfSupervisor creates the actor
          from the Props and supervises it, and does use a delay mechanism to prevent
          fast restarts.
          
          
# akka http
* The Akka HTTP modules implement a full server- and client-side HTTP stack on top of akka-actor and akka-stream
* The high-level, routing API of Akka HTTP provides a DSL to describe HTTP “routes” and how they should be handled
* Marshalling
    * Transforming request and response bodies between over-the-wire formats and objects to be used in your 
    application is done separately from the route declarations, in marshallers, which are pulled in implicitly 
    using the “magnet” pattern. 
    * This means that you can complete a request with any kind of object as long as there is an implicit marshaller 
    available in scope.
    * Marshalling is the process of converting a higher-level (object) structure into some kind of lower-level 
    representation, often a “wire format”. Other popular names for marshalling are “serialization” or “pickling”.
* unmarshalling
    * “Unmarshalling” is the process of converting some kind of a lower-level representation, often a “wire format”, into a higher-level (object) structure. Other popular names for it are “Deserialization” or “Unpickling”.
* Timeouts
    * idle-timeout is a global setting which sets the maximum inactivity time of a given connection
        * In other words, if a connection is open but no request/response is being written to it for over idle-timeout time, the connection will be automatically closed.
            ```
            akka.http.server.idle-timeout
            akka.http.client.idle-timeout
            akka.http.host-connection-pool.idle-timeout
            akka.http.host-connection-pool.client.idle-timeout
            ```
        * Request timeouts are a mechanism that limits the maximum time it may take to produce an HttpResponse from a route
            * If that deadline is not met the server will automatically inject a Service Unavailable HTTP response and close the connection to prevent it from leaking and staying around indefinitely (for example if by programming error a Future would never complete, never sending the real response otherwise).
* Server API
    * Akka HTTP also provides an embedded, Reactive-Streams-based, fully asynchronous HTTP/1.1 server implemented on top of Streams.
    * The “Route” is the central concept of Akka HTTP’s Routing DSL
        * type Route = RequestContext => Future[RouteResult]
    * Generally when a route receives a request (or rather a RequestContext for it) it can do one of these things:
        * Complete the request by returning the value of requestContext.complete(...)
        * Reject the request by returning the value of requestContext.reject(...) (see Rejections)
        * Fail the request by returning the value of requestContext.fail(...) or by just throwing an exception (see Exception Handling)
        * Do any kind of asynchronous processing and instantly return a Future[RouteResult] to be eventually completed later