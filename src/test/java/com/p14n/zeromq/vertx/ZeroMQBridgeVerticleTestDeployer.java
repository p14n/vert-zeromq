package com.p14n.zeromq.vertx;

import org.junit.Assert;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.testframework.TestClientBase;
import org.zeromq.ZMQ;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 29/10/2013
 */
public class ZeroMQBridgeVerticleTestDeployer extends TestClientBase {

    public static void main(String[] args){
        performClientTests();
    }

  @Override
    public void start() {
        super.start();
        tu.appReady();
    }

    public void testZeroMQVerticle() {

        JsonObject json = new JsonObject();
        json.putString("address","tcp://*:5558");

        container.deployWorkerVerticle(ZeroMQBridgeVerticle.class.getName(), json, 1, true,
                new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> res) {
                if (res.succeeded()) {
                    performClientTests();
                } else {
                    Assert.fail("Deployment failed "+res.result());
                }
                tu.testComplete();
            }
        });
    }

    private static void performClientTests() {

        final String address = "tcp://localhost:5558";
        final ZMQ.Context ctx = ZMQ.context(1);
        final ZMQ.Socket registered = ctx.socket(ZMQ.DEALER);
        registered.connect(address);
        registered.send("register:testHandler".getBytes());

        System.out.println("registered");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
        client.connect(address);
        client.send("testHandler".getBytes(), ZMQ.SNDMORE);
        client.send("oh".getBytes(), 0);

        System.out.println("sent");

        byte[] response = registered.recv();
        byte[] replyaddress = registered.recv();
        Assert.assertEquals("oh", new String(response));
        System.out.println("received");

        registered.send(replyaddress, ZMQ.SNDMORE);
        registered.send("hai".getBytes(), 0);

        System.out.println("replied");

        byte[] response3 = client.recv();
        Assert.assertEquals("hai", new String(response3));
        System.out.println("complete");

        try {
            Thread.sleep(6000); //Lets see those timeout warnings
        } catch (InterruptedException e) {

        }
    }

}
