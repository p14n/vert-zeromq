package com.p14n.zeromq.vertx;

import com.p14n.zeromq.AsyncRouter;
import com.p14n.zeromq.MessageResponder;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class VertxRouter extends AsyncRouter {

    EventBus bus;

    public VertxRouter(String address, EventBus bus) {
        super(address);
        this.bus = bus;
    }

    @Override
    protected void handleBlockingRequest(byte[] handler, byte[] msg,final MessageResponder messageResponder) {
        bus.send(new String(handler),msg,new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                messageResponder.respond(message.body());
            }
        });
        System.out.println("sent to VERT " + new String(msg));

    }
}
