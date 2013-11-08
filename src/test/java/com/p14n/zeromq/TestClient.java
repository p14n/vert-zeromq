package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class TestClient implements Runnable {

    private static Random rand = new Random(System.nanoTime());
    byte[] handler;
    String address;
    ZMQ.Socket client;
    ZMQ.Context ctx;
    boolean running = true;
    CountDownLatch latch;

    public TestClient(String address, int iterations) {
        this.address = address;
        latch = new CountDownLatch(iterations);
    }

    public void waitFor() throws TimeoutException {
        waitFor(30);
    }

    public void waitFor(int secs) throws TimeoutException {
        try {
            latch.await(secs, TimeUnit.SECONDS);
            if (latch.getCount() != 0)
                throw new TimeoutException("Not all messages received after " + secs + " seconds");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHandler(String handler) {
        this.handler = handler.getBytes();
    }

    public void run() {
        try {
            ctx = ZMQ.context(1);
            client = ctx.socket(ZMQ.DEALER);

            //  Set random identity to make tracing easier
            String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
            client.setIdentity(identity.getBytes());
            client.connect(address);

            ZMQ.Poller poller = new ZMQ.Poller(1);
            poller.register(client, ZMQ.Poller.POLLIN);

            int requestNbr = 0;
            Set<String> msgOut = new HashSet<String>();

            while (running) {

                requestNbr = send(identity, requestNbr, msgOut);

                //  Tick once per second, pulling in arriving messages
                poller.poll(100);
                if (poller.pollin(0)) {

                    byte msg[] = client.recv(0);
                    String compare = new String(msg);
                    if(client.hasReceiveMore()){
                        client.recv(0); //not interested in replying
                    }
                    if (msgOut.contains(compare)) {
                        latch.countDown();
                        msgOut.remove(compare);
                    } else {
                        System.out.println(identity + " received unexpected " + compare);
                    }
                    if (latch.getCount() == 0) {
                        running = false;
                        break;
                    }

                }
            }
        } finally {
            try {
                client.close();
            } catch (Exception e) {

            }
            try {
                ctx.term();
            } catch (Exception e) {

            }

        }
    }

    private int send(String identity, int requestNbr, Set<String> msgOut) {
        String out = String.format(identity + " request #%d", ++requestNbr);
        msgOut.add(out);
        if (handler != null)
            client.send(handler, ZMQ.SNDMORE);
        client.send(out, 0);
        return requestNbr;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
