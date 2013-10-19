package com.p14n.zeromq.vertx;

import io.netty.channel.EventLoop;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultContext;
import org.vertx.java.platform.impl.WrappedVertx;
import org.zeromq.ZMQ;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 18/10/2013
 */
public class AsyncSocket {

    public AsyncSocket(final String address, final Vertx vertx) {

        final WrappedVertx w = ((WrappedVertx) vertx);
        final DefaultContext context = ((WrappedVertx) vertx).getOrCreateContext();
        final EventLoop loop = context.getEventLoop();

        w.getBackgroundPool().submit(new Runnable() {

            @Override
            public void run() {
                ZMQ.Context c = ZMQ.context(1);

                final ZMQ.Socket server = c.socket(ZMQ.ROUTER);
                server.bind(address);

                ZMQ.Poller poller = new ZMQ.Poller(1);
                poller.register(server, ZMQ.Poller.POLLIN);

                while (!Thread.currentThread().isInterrupted()) {

                    poller.poll();

                    if (poller.pollin(0)) {

                        context.execute(loop,
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        final byte[][] results = new byte[3][];
                                        System.out.println("loop " + w.isEventLoop());
                                        System.out.println("Reading from thread " + Thread.currentThread().getId());
                                        int resultIndex = 0;
                                        // receive message
                                        results[resultIndex++] = server.recv(0);

                                        while (server.hasReceiveMore()) {
                                            results[resultIndex++] = server.recv(0);
                                        }
                                        //send to bus
                                        handleRead(results, vertx, new ContextSocketResponder(context, loop,
                                                results[0],
                                                server));
                                    }
                                });
                    }
                }
                server.close();
                c.term();

            }
        });

    }

    protected void handleRead(final byte[][] results, Vertx vertx, final ContextSocketResponder responder) {
        vertx.eventBus().send(new String(results[1]), results[2], new Handler<Message<byte[]>>() {
            @Override
            public void handle(final Message<byte[]> message) {
                responder.respond(message.body());
            }
        });
    }
}
