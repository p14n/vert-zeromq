package com.p14n.zeromq;

import com.p14n.zeromq.vertx.VertxRouter;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import java.util.concurrent.TimeoutException;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class VertxTest extends TestVerticle {

    @Test
    public void shouldGetMessagesBackFromAVertHandler() {

        final VertxRouter r = new VertxRouter("tcp://*:5558", vertx.eventBus());
        r.start();

        final TestClient client = new TestClient("tcp://localhost:5558", 5);
        client.setHandler("test");
        new Thread(client).start();

        vertx.eventBus().registerHandler("test", new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                System.out.println("VERT received " + new String(message.body()));
                message.reply(message.body());
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.waitFor();
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
                VertxAssert.testComplete();
                r.stop();
            }
        }).start();
    }
}