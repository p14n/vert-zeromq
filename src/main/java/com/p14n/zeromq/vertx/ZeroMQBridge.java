package com.p14n.zeromq.vertx;

import com.p14n.zeromq.*;
import org.vertx.java.core.*;
import org.vertx.java.core.eventbus.*;

import java.util.*;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class ZeroMQBridge extends AsyncRouter {

    private final static String reg = "register:";
    private final static int reglength = reg.length();
    private final static String unreg = "unregister:";
    private final static int unreglength = unreg.length();
    Map<String, Handler> zmqHandlers = new HashMap<>();
    Vertx vertx;

    public ZeroMQBridge(String address, Vertx vertx) {
        super(address);
        this.vertx = vertx;
        handleRequest(new RequestHandler() {
            @Override
            public void handleRequest(byte[][] message, final MessageResponder responder) {

                if (message.length == 2) {
                    handleCommand(message, responder);
                } else {
                    eventBus().send(new String(message[1]), message[2], new Handler<Message<byte[]>>() {
                        @Override
                        public void handle(Message<byte[]> message) {
                            String replyAddress = message.replyAddress();
                            if (replyAddress == null) {
                                responder.respond(message.body());
                            } else {
                                responder.respond(message.body(), replyAddress.getBytes());
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void run(final Runnable runnable) {
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                runnable.run();
            }
        });
    }

    private void handleCommand(byte[][] message, final MessageResponder responder) {
        String command = new String(message[1]);
        if (command.startsWith(reg)) {
            final String handler = command.substring(reglength);
            Handler h = new Handler<Message<byte[]>>() {
                @Override
                public void handle(Message<byte[]> message) {
                    if (message.replyAddress() != null) {
                        responder.respond(message.body(), message.replyAddress().getBytes());
                    } else {
                        responder.respond(message.body());
                    }
                }
            };
            zmqHandlers.put(handler, h);
            eventBus().registerHandler(handler, h, new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> event) {
                    if (event.succeeded()) {
                        info("Registered 0mq handler " + handler);
                    } else {
                        error("Register 0mq handler " + handler + " failed", event.cause());
                    }
                }
            });
        } else if (command.startsWith(unreg)) {
            String handler = command.substring(unreglength);
            if (handler != null && zmqHandlers.containsKey(handler)) {
                eventBus().unregisterHandler(handler, zmqHandlers.get(handler));
                zmqHandlers.remove(handler);
            }
        }
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }
}
