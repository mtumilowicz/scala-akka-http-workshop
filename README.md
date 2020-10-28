# scala-akka-http-actor-workshop
* https://medium.com/se-notes-by-alexey-novakov/crud-microservice-with-akkahttp-c914059bcf9f
* https://www.manning.com/books/akka-in-action
* https://doc.akka.io/docs/akka-http/10.2.1/

# preface
* two ultimate goals: 
    1. complexity has to stay as low as possible
    1. resources must be used efficiently while you scale the application.
* actors vs synchronous approach
    * tabelę zrobić
    * scaling
        * mix of threads, shared mutable state in a CRUD database, and web service RPC calls
        * send and receive messages, no shared mutable state, immutable log of events
    * providing interactive information
        * poll for current information 
        * event-driven: push when the event occurs
    * scaling out on the network
        * synchronous RPC, blocking I/O
        * asynchronous messaging, nonblocking I/O
    * handling failures
        * handle all exceptions; only continue if everything works
        * let it crash, isolate failure, and continue without failing parts
* The actor model chooses the abstraction of send-
  ing and receiving messages to decouple from the number of threads or the number of
  servers that are being used.
* If we want the application to scale to many servers, there’s an important requirement
  for the programming model: it will have to be asynchronous, allowing components to
  continue working while others haven’t responded yet
## actor operations
* An actor is a lightweight process that has only four core operations: create, send, become, and supervise. 
    * All of these operations are asynchronous.
* SEND
    * An actor can only communicate with another actor by sending it messages. 
        * This takes encapsulation to the next level.
            * in objects we can specify which methods can be publicly called and which state is accessible 
            from the outside.
            * Actors don’t allow any access to internal state
    * Sending messages is always asynchronous, in what is called a fire and forget style.
        * If it’s important to know
          that another actor received the message, then the receiving actor should just send
          back an acknowledgement message of some kind.
    * The order of messages is only guaranteed per sending actor, so if many
    users edit the same message in a conversation, the final result can
    vary depending on how the messages are interleaved over time.
* CREATE
    * An actor can create other actors
    * this automatically creates a hierarchy of actors
* BECOME
    * State machines are a great tool for making sure that a system only executes particular
      actions when it’s in a specific state.
    * Actors receive messages one at a time, which is a convenient property for imple-
      menting state machines
        * An actor can change how it handles incoming messages by swapping out its behavior.
            * Imagine that users want to be able to close a Conversation
                * The Conversation starts out in a started state and becomes closed when a CloseConversation 
                is received. 
                * Any message that’s sent to the closed Conversation could be ignored. 
                * The Conversation swaps its behavior from adding messages to itself to ignoring all messages.
* SUPERVISE
    * An actor needs to supervise the actors that it creates
        * The supervisor in the chat application can keep track of what’s happening to the main components
    * The Supervisor decides what should happen when components fail in the system
        * The Supervisor gets notified with special messages that indicate which actor has
          crashed, and for what reason. 
        * The Supervisor can decide to restart an actor or take the actor out of service.
            * it could decide to take the OutlookContacts actor out of service because it failed
              too often
    * Any actor can be a supervisor, but only for actors that it creates itself
    * Actors: decoupled on three axes
        * Decoupling on exactly these three axes is important because this is exactly the flex-
          ibility that’s required for scaling
          * Space/Location
            * An actor gives no guarantee and has no expectation about where
              another actor is located.
          * Time
            * An actor gives no guarantee and has no expectation about when its
              work will be done.
          * Interface
            * An actor has no defined interface
            * An actor has no expectation about which messages other components can understand
            * Nothing is shared between actors; 
            * actors never point to or use a shared piece of information that changes in place. 
            * Information is passed in messages.
        * Coupling components in location, time, and interface is the biggest impediment to
          building applications that can recover from failure and scale according to demand
        * A system built out of components that are coupled on all three axes can only exist on
          one runtime and will fail completely if one of its components fails.
## constructs
* 1.7.1 ActorSystem
    * Actors can create other actors, but who creates the first one?
        * The first thing that every Akka application does is create an ActorSystem
    * The actor system can create so called top-level actors, and it’s a common pattern to
    create only one top-level actor for all actors in the application—in our case, the
    Supervisor actor that monitors everything.
    * remoting and a journal for durability
        * The ActorSystem is also the nexus for these support capabilities
    * A simple example of a support capability is the scheduler, which can send messages to actors periodically
    * An ActorSystem returns an address to the created top-level actor instead of the actor itself. 
        * This address is called an ActorRef
        * The ActorRef can be used to send messages to the actor. 
        * This makes sense when you think about the fact that the actor could be on another server.
    * Sometimes you’d like to look up an actor in the actor system. 
        * This is where ActorPaths come in. 
        * You could compare the hierarchy of actors to a URL path structure.
        * Every actor has a name. 
        * This name needs to be unique per level in the hierarchy: two sibling actors can’t have the same 
        name
        * if you don’t provide a name, Akka generates one for you, but it’s a good idea to name all your actors
        * All actor references can be located directly by an actor path, absolute or relative.
* 1.7.2 ActorRef, mailbox, and actor
    * Messages are sent to the actor’s ActorRef 
    * Every actor has a mailbox—it’s a lot like a queue
    * Messages sent to the ActorRef will be temporarily stored in the mailbox to be
      processed later, one at a time, in the order they arrived
    * overview: ActorRef -> Mailbox -> Actor
* 1.7.3 Dispatchers
    * Actors are invoked at some point by a dispatcher
        * The dispatcher pushes the messages in the mailbox through the actors, so to speak
    * So when you send a message to an actor, all you’re really doing is leaving a message behind in its mailbox. 
        * Eventually a dispatcher will push it through the actor.
    * Actors are lightweight because they run on top of dispatchers;
        * the actors aren’t necessarily directly proportional to the number of threads
        * Akka actors take a lot less space than threads: around 2.7 million actors can fit in 1 GB of memory. 
        * That’s a big difference compared to 4096 threads for 1 GB of memory
    * CALLBACK HELL
        * A lot of frameworks out there provide asynchronous pro-
          gramming through callbacks
        * Callback Hell, where every callback calls another callback, which calls another callback, and so on.
        * Actors simply drop off messages in mailboxes and let the dispatcher sort out the rest.
* 1.7.4 Actors and the network
    * How do Akka actors communicate with each other across the network? 
        * ActorRefs are essentially addresses to actors, so all you need to change is how the addresses are
        linked to actors
        * Akka provides a remoting module that enables the transparency you seek
* 4.1.2 Let it crash
    * Akka Actor provides two separate flows: one for normal logic and one for fault recovery logic
    * The normal flow consists of actors that handle normal messages; the recovery
      flow consists of actors that monitor the actors in the normal flow
    * Actors that monitor other actors are called supervisors
    * Instead of catching exceptions in an actor, we’ll just let the actor crash
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