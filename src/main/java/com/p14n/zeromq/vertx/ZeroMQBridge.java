package com.p14n.zeromq.vertx;

import com.p14n.zeromq.AsyncRouter;
import com.p14n.zeromq.MessageResponder;
import com.p14n.zeromq.RequestHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class ZeroMQBridge extends AsyncRouter {

    EventBus bus;

    public ZeroMQBridge(String address, EventBus bus) {
        super(address);
        this.bus = bus;
        handleRequest(new RequestHandler() {
            @Override
            public void handleRequest(byte[][] message, final MessageResponder responder) {
                eventBus().send(new String(message[1]), message[2], new Handler<Message<byte[]>>() {
                    @Override
                    public void handle(Message<byte[]> message) {
                        String replyAddress = message.replyAddress();
                        if(replyAddress==null){
                            responder.respond(message.body());
                        } else {
                            responder.respond(message.body(),replyAddress.getBytes());
                        }
                    }
                });
            }
        });
    }

    public EventBus eventBus(){
        return bus;
    }
}
