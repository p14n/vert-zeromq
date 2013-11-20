vert-zeromq
===========

![Build status](https://travis-ci.org/p14n/vert-zeromq.png)

Providing a bridge from zero-mq to the vert-x event bus.

Available in the module registry as p14n~vert-zeromq~0.0.1.  Note this release is not production ready.  1.0.0 will be
released with vert.x 2.1 final, and will be production ready.

This module enables you to remotely call a handler on the bus, receive replies, and reply back.  It also allows you
to register a 0mq socket as a handler, receive calls to that handler address, and reply back.


If a reply handler was supplied by the sender of the message received at the socket,
that handler's address is included as the second frame of the message.

NOTE - you cannot currently reply to a 0mq socket that has registered as a handler (although it can reply to you).

Config requires the address the module should listen on:

```json
{
 "address":"tcp://*:5558"
}
```

* Send a message to an event bus handler by sending the handler address as the first frame, the message as the second.
* Reply to a handler by using its address (provided in the second frame of the message).
* Register a 0mq handler by sending a single message 'register:myHandlerName'
* Unregister a 0mq handler by sending a single message 'unregister:myHandlerName'.


### Calling an event bus handler

```java
ZMQ.Context ctx = ZMQ.context(1);

ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
client.connect("tcp://localhost:5558");
client.send("echoHandler".getBytes(), ZMQ.SNDMORE); //Send the handler address
client.send("hello".getBytes(), 0); //Send the message

byte[] response = client.recv(); //Get the response
Assert.assertEquals("hello", new String(response));
```
### Registering a socket as a handler, sending to it and receiving a reply

```java
String address = "tcp://localhost:5558";
ZMQ.Context ctx = ZMQ.context(1);

ZMQ.Socket registered = ctx.socket(ZMQ.DEALER); //This will be our handler
registered.connect(address);
registered.send("register:testHandler".getBytes()); //Register this socket as handler 'testHandler'

Thread.sleep(100); //Messages sent immediately could be dropped in the bus

ZMQ.Socket client = ctx.socket(ZMQ.DEALER); //This socket will call our handler socket
client.connect(address);
client.send("testHandler".getBytes(), ZMQ.SNDMORE); //Send the handler address
client.send("oh".getBytes(), 0); //Send the message

byte[] response = registered.recv(); //Get the message sent to the handler socket by the client 'oh'
byte[] replyaddress = registered.recv(); //Get the client socket's handler address
assertEquals("oh", new String(response));

registered.send(replyaddress, ZMQ.SNDMORE); //Send the address of the client socket
registered.send("hai".getBytes(), 0); //Send a reply message

byte[] response3 = client.recv(); //Get the response sent to the client by the handler socket
assertEquals("hai", new String(response3));
```
---
For more on 0mq, see the excellent guide at <http://zguide.zeromq.org/page:all>

