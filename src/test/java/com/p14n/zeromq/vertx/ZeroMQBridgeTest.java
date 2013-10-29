package com.p14n.zeromq.vertx;

import com.p14n.zeromq.TestClient;
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
public class ZeroMQBridgeTest extends TestVerticle {

    private TestClient createAndStartClient(){
        TestClient client = new TestClient("tcp://localhost:5558", 10);
        client.setHandler("test");
        new Thread(client).start();
        return client;
    }

    private TestClient[] createClients(int count){
        TestClient[] clients = new TestClient[count];
        for(int i=0;i<count;i++){
            clients[i] = createAndStartClient();
        }
        return clients;
    }

    public void shouldGetMessagesBackFromAVertHandler() {

        final ZeroMQBridge r = new ZeroMQBridge("tcp://*:5558", vertx);
        r.start();

        final TestClient[] clients = createClients(10);

        vertx.eventBus().registerHandler("test", new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                message.reply(message.body());
            }
        });
        final long start = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(TestClient c:clients)
                        c.waitFor(10);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
                System.out.print("a Took "+(System.currentTimeMillis()-start));
                VertxAssert.testComplete();
                r.stop();
            }
        }).start();
    }


    @Test
    public void test1(){
        shouldGetMessagesBackFromAVertHandler();
    }
    @Test
    public void test3(){
        shouldGetMessagesBackFromAVertHandler();
    }
   /* @Test
    public void test5(){
        shouldGetMessagesBackFromAVertHandler();
    }
    @Test
    public void test6(){
        shouldGetMessagesBackFromIOOnEventLoop();
    }*/
}