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
* Differences between approaches
    * Scaling
        * Use a mix of threads, shared
          mutable state in a database (Cre-
          ate, Insert, Update, Delete), and
          web service RPC calls for scaling.
        * Actors send and receive messages.
          No shared mutable state. Immuta-
          ble log of events.
    * Providing interactive information
        * Poll for current information. 
        * Event-driven: push when the event occurs.
    * Scaling out on the network
        * Synchronous RPC, blocking I/O.
        * Asynchronous messaging, nonblocking I/O.
    * Handling failures
        * Handle all exceptions; only continue if everything works.
        * Let it crash. Isolate failure, and continue without failing parts.
* event-driven approach has a couple of advantages:
  * It minimizes direct dependencies between components. 
    * The conversation doesn’t know about the Mentions object and could not care less what happens with the event. 
    * The conversation can continue to operate when the Mentions object crashes.
  * The components of the application are loosely coupled in time. 
    * It doesn’t matter if the Mentions object gets the events a little later, as long as it gets the
    events eventually.
  * The components are decoupled in terms of location. 
    * The Conversation and Mentions object can reside on different servers; the events are just messages
    that can be transmitted over the network.
* A concurrent system is not by definition a parallel system. 
    * Concurrent processes can, for example, be executed on one CPU through the use of time slicing, 
    where every process gets a certain amount of time to run on the CPU , one after another.
* The actor model chooses the abstraction of send-
  ing and receiving messages to decouple from the number of threads or the number of
  servers that are being used.
* If we want the application to scale to many servers, there’s an important requirement
  for the programming model: it will have to be asynchronous, allowing components to
  continue working while others haven’t responded yet, as in the chat application
* 1.6.2 Actor operations
    * An actor is a lightweight process that has only four core operations: create, send, become, and supervise. 
        * All of these operations are asynchronous.
    * SEND
        * An actor can only communicate with another actor by sending it messages. 
            * This takes encapsulation to the next level.
            * in objects we can specify which methods can be publicly called and which state is accessible 
            from the outside.
                * Actors don’t allow any access to internal state
        * Actors can’t share mutable state
        * Sending messages is always asynchronous, in what is called a fire and forget style.
            * If it’s important to know
              that another actor received the message, then the receiving actor should just send
              back an acknowledgement message of some kind.
        * WHAT, NO TYPE SAFETY?
            * Actors can receive any message, and you can send
              any message you want to an actor (it just might not process the message)
            * This basically means that type checking of the messages that are sent and received
              is limited
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
