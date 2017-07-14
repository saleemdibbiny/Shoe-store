# Shoe-store #
<b>SPL: Assignment 2</b><br />
Java Concurrency and Synchronization<br /><br />

## Description ##
Implemention of a simple shoe store using an implementation of a simple micro-service framework. The micro-services architecture has become quite
popular in recent years. In the micro-services architecture, complex applications are composed of
small, independent services which have the ability to communicate with each other using broadcastmessages
and requests. The micro-service architecture allows us to compose a large program from a
lot of small independant parts. This not only allows for better testability and much clearer seperation
of concerns, but it also allows to replace, add and remove different parts of the system without
breaking the system as a whole or even shutting it down.

## Implementation ##
The implementation is composed of two parts: <br />
1. Building a simple but powerful micro-service framework <br />
2. Implementing a simple shoe store application on top of this framework<br />

###  Part 1: Micro-Services Framework Architecture ###
In this part, we built a simple micro-services framework. A micro-services framework is composed
of two main parts, Micro-Services and a MessageBus. Each micro-service is a thread that can
send and receive messages using a shared object referred to as the message-bus.
###  Part 2: Building an Online Shoe Store ###
Using the micro-services framework we built in order to
implement the functionality needed for a simple online shoe store with simulated clients. <br />
For the purpose of the simulative parts of the system, we implemented a time service. The
time service keeps track of time passing in our execution, it serves as a global clock that counts the
number of clock ticks that have passed and broadcast TickBroadcast messages (which contains the
number of the current tick).

