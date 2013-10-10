package com.p14n.zeromq;

import com.p14n.zeromq.vertx.VertxRouter;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class VertxTest extends TestVerticle {

    VertxRouter r;

    @Override
    public void stop() {
        r.stop();
        super.stop();
    }

    @Override
    public void start() {
        r = new VertxRouter("tcp://*:5558", getVertx().eventBus());
        r.start();

        getVertx().eventBus().registerHandler("test", new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                System.out.println("VERT received " + new String(message.body()));
                message.reply(message.body());
            }
        });
        super.start();
    }

    @Test
    public void shouldGetMessagesBackFromAVertHandler() {
        TestClient client = new TestClient("tcp://localhost:5558", 5);
        client.setHandler("test");
        new Thread(client).start();
        client.waitFor();

    }

}
