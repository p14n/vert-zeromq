package com.p14n.zeromq.vertx;

import com.p14n.zeromq.AsyncRouter;
import com.p14n.zeromq.MessageResponder;
import com.p14n.zeromq.RequestHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.eventbus.ReplyFailure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    Set<byte[]> handlerSocketids = new HashSet<>();
    Vertx vertx;

    public ZeroMQBridge(String address, Vertx vertx) {
        this(address, vertx, 5000);
    }


    public ZeroMQBridge(String address, Vertx vertx, final int responseTimeout) {
        super(address);
        this.vertx = vertx;
        setRequestHandler(new RequestHandler() {
            @Override
            public void handleRequest(byte[][] message, final MessageResponder responder) {

                if (message.length == 2) {
                    handleCommand(message, responder);
                } else {
                    if (handlerSocketids.contains(responder.getSocketId())) {
                        eventBus().send(new String(message[1]), message[2]);
                    } else {
                        boolean IWantToHaemorrageMemory = true;
                        if (!IWantToHaemorrageMemory) {
                            eventBus().sendWithTimeout(new String(message[1]), message[2], responseTimeout,
                                    new AsyncResultHandler<Message<byte[]>>() {
                                        @Override
                                        public void handle(AsyncResult<Message<byte[]>> event) {
                                            if (event.succeeded()) {
                                                Message<byte[]> message = event.result();
                                                String replyAddress = message.replyAddress();
                                                if (replyAddress == null) {
                                                    responder.respond(message.body());
                                                } else {
                                                    responder.respond(message.body(), replyAddress.getBytes());
                                                }
                                            } else {
                                                if (event.cause() instanceof ReplyException) {
                                                    ReplyException ex = (ReplyException) event.cause();
                                                    if (ReplyFailure.NO_HANDLERS.equals(ex.failureType())) {
                                                        error("Send failed", event.cause());
                                                        responder.respond(ex.failureType().name().getBytes());
                                                    }
                                                }
                                            }
                                        }
                                    });
                        } else {
                            eventBus().send(new String(message[1]), message[2],
                                    new Handler<Message<byte[]>>() {
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
            unregister(handler);
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
            unregister(handler);
        }
    }

    private void unregister(String handler) {
        if (handler != null && zmqHandlers.containsKey(handler)) {
            eventBus().unregisterHandler(handler, zmqHandlers.get(handler));
            zmqHandlers.remove(handler);
            info("Unregistered 0mq handler " + handler);
        }
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }
}
