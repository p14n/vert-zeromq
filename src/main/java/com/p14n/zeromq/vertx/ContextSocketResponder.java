package com.p14n.zeromq.vertx;

import io.netty.channel.EventLoop;
import org.vertx.java.core.impl.DefaultContext;
import org.zeromq.ZMQ;

public class ContextSocketResponder {

    final ZMQ.Socket server;
    EventLoop loop;
    byte[] id;
    DefaultContext c;

    public ContextSocketResponder(final DefaultContext c,EventLoop loop, byte[] id, ZMQ.Socket server) {
        this.loop = loop;
        this.id = id;
        this.server = server;
        this.c=c;
    }

    public void respond(final byte msg[]) {
        c.execute(loop,new Runnable() {
            @Override
            public void run() {
                System.out.println("Sending from thread " + Thread.currentThread().getId());
                server.send(id, ZMQ.SNDMORE);
                server.send(msg, 0);
                System.out.println("Sent " + new String(msg) + " to " + new String(id));
            }
        });
/*        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
            }
        });*/
    }
}