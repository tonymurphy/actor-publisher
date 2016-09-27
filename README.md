Introduction
============

Attempting to Create a Reactive Streams Publisher using Akka Streams ActorPublisher

Problems - While Publisher calls onNext message, and I believe subscription is setup properly, 
however, I don't see any messages come throught on the Subscriber (via console logging)

Also an error message occurs, and I don't know how to debug this
error akka.stream.AbruptTerminationException: Processor actor [Actor[akka://MySpec/user/StreamSupervisor-0/flow-0-0-map#1732708410]] terminated abruptly
