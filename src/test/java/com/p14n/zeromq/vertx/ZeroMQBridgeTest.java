package com.p14n.zeromq.vertx;

import com.p14n.zeromq.*;
import org.junit.*;

import java.util.concurrent.*;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
@Ignore("No longer works as the verticle needs to be a worker - needs rewriting")
public class ZeroMQBridgeTest {

    public static void main(String ars[]){
        new ZeroMQBridgeTest().shouldGetMessagesBackFromAVertHandler();
    }

    private TestClient createAndStartClient(){
        TestClient client = new TestClient("tcp://localhost:5558", 10);
        client.setHandler("echo");
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

        final TestClient[] clients = createClients(100);

        final long start = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(TestClient c:clients)
                        c.waitFor(100);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
                System.out.print("a Took "+(System.currentTimeMillis()-start));

            }
        }).start();
    }


}