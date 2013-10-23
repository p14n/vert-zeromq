package com.p14n.zeromq.vertx;

import com.p14n.zeromq.AsyncRouter;
import com.p14n.zeromq.MessageResponder;
import com.p14n.zeromq.RequestHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class ZeroMQBridge extends AsyncRouter {

    EventBus bus;

    Map<String,Handler> zmqHandlers = new HashMap<>();

    public ZeroMQBridge(String address, EventBus bus) {
        super(address);
        this.bus = bus;
        handleRequest(new RequestHandler() {
            @Override
            public void handleRequest(byte[][] message, final MessageResponder responder) {

                if (message.length == 2) {
                    handleCommand(message,responder);
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

    private final static String reg = "register:";
    private final static int reglength = reg.length();
    private final static String unreg = "unregister:";
    private final static int unreglength = unreg.length();

    private void handleCommand(byte[][] message, final MessageResponder responder) {
        String command = new String(message[1]);
        if(command.startsWith(reg)){
            String handler = command.substring(reglength);
            Handler h = new Handler<Message<byte[]>>() {
                @Override
                public void handle(Message<byte[]> message) {
                    responder.respond(message.body());
                }
            };
            zmqHandlers.put(handler,h);
            bus.registerHandler(handler,h);
        } else if(command.startsWith(unreg)){
            String handler = command.substring(unreglength);
            if(handler!=null&&zmqHandlers.containsKey(handler)){
                bus.unregisterHandler(handler,zmqHandlers.get(handler));
                zmqHandlers.remove(handler);
            }
        }
    }

    public EventBus eventBus() {
        return bus;
    }
}
