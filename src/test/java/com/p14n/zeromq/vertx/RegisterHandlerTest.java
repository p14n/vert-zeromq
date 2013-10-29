package com.p14n.zeromq.vertx;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import org.zeromq.ZMQ;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 26/10/2013
 */
@Ignore
public class RegisterHandlerTest extends TestVerticle {

    @Test
    public void shouldReceiveMessageAtRegisteredHandler() {

        final ExecutorService service = Executors.newCachedThreadPool();

        final ZeroMQBridge r = new ZeroMQBridge("tcp://*:5558", vertx);
        r.start();
        final CyclicBarrier b = new CyclicBarrier(2);
        final String address = "tcp://localhost:5558";

        service.submit(new Runnable() {
            @Override
            public void run() {

                final ZMQ.Context ctx = ZMQ.context(1);
                final ZMQ.Socket registered = ctx.socket(ZMQ.DEALER);
                registered.connect(address);
                registered.send("register:test".getBytes());

                System.out.println("registered");
                pause(1000);

                final ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
                client.connect(address);
                client.send("test".getBytes(), ZMQ.SNDMORE);
                client.send("oh".getBytes(), 0);

                System.out.println("sent");
                pause(100);

                byte[] response = registered.recv();
                byte[] response2 = registered.recv();
                Assert.assertEquals("oh", new String(response));
                System.out.println("received");
                pause(100);

                registered.send(response2, ZMQ.SNDMORE);
                registered.send("hai".getBytes(), 0);

                System.out.println("replied");
                pause(100);

                byte[] response3 = client.recv();
                Assert.assertEquals("hai", new String(response3));
                System.out.println("complete");
                VertxAssert.testComplete();
                try {
                    b.await();
                } catch (Exception e) {
                }


            }
        });
        try {
            b.await();
        } catch (Exception e) {
        }
        r.stop();

    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("", e);
        }
    }

}
