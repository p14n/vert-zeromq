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

    private TestClient createAndStartClient(){
        TestClient client = new TestClient("tcp://localhost:5558", 1000);
        client.setHandler("test");
        new Thread(client).start();
        return client;
    }

    @Test
    public void shouldGetMessagesBackFromAVertHandler() {

        final VertxRouter r = new VertxRouter("tcp://*:5558", vertx.eventBus());
        r.start();

        final TestClient client1 = createAndStartClient();
        final TestClient client2 = createAndStartClient();
        final TestClient client3 = createAndStartClient();
        final TestClient client4 = createAndStartClient();
        final TestClient client5 = createAndStartClient();

        vertx.eventBus().registerHandler("test", new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                message.reply(message.body());
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client1.waitFor();
                    client2.waitFor();
                    client3.waitFor();
                    client4.waitFor();
                    client5.waitFor();
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
                VertxAssert.testComplete();
                r.stop();
            }
        }).start();
    }
}