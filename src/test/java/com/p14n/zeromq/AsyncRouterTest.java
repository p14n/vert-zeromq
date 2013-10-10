package com.p14n.zeromq;

import org.junit.Test;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class AsyncRouterTest {

    @Test
    public void shouldReceiveAtLeastASingleResponse(){

        AsyncRouter echo = new AsyncRouter("tcp://*:5558") {
            @Override
            protected void handleBlockingRequest(byte[] handler,byte[] msg, MessageResponder messageResponder) {
                messageResponder.respond(msg);
            }
        };
        echo.start();

        TestClient client = new TestClient("tcp://localhost:5558",1);
        new Thread(client).start();
        client.waitFor();

    }
    @Test
    public void shouldReceiveCorrelatedResponses(){

        AsyncRouter echo = new AsyncRouter("tcp://*:5558") {
            @Override
            protected void handleBlockingRequest(byte[] handler,byte[] msg, MessageResponder messageResponder) {
                messageResponder.respond(msg);
            }
        };
        echo.start();

        TestClient client3 = new TestClient("tcp://localhost:5558",5);
        new Thread(client3).start();

        TestClient client2 = new TestClient("tcp://localhost:5558",5);
        new Thread(client2).start();

        TestClient client1 = new TestClient("tcp://localhost:5558",5);
        new Thread(client1).start();

        client3.waitFor();
        client2.waitFor();
        client1.waitFor();

    }
}
