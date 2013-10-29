package com.p14n.zeromq;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class AsyncRouterTest {

    @Test
    public void shouldReceiveAtLeastASingleResponse() throws TimeoutException {

        AsyncRouter echo = new AsyncRouter("tcp://*:5558")
                .handleRequest(new RequestHandler() {
                    @Override
                    public void handleRequest(byte[][] message, MessageResponder responder) {
                        responder.respond(message[1]);
                    }
                });
        echo.start();

        TestClient client = new TestClient("tcp://localhost:5558",1);
        new Thread(client).start();
        client.waitFor();
        System.out.println("Test done");
        echo.stop();

    }
    @Test
    public void shouldReceiveCorrelatedResponses() throws TimeoutException {

        AsyncRouter echo = new AsyncRouter("tcp://*:5558").handleRequest(new RequestHandler() {
            @Override
            public void handleRequest(byte[][] message, MessageResponder responder) {
                responder.respond(message[1]);
            }
        });
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

        echo.stop();

    }
}
