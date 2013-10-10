package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class TestClient implements Runnable {

    public TestClient(String address,int iterations) {
        this.address = address;
        latch = new CountDownLatch(iterations);
    }

    public void waitFor(){
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHandler(String handler) {
        this.handler = handler.getBytes();
    }

    byte[] handler;
    String address;
    private static Random rand = new Random(System.nanoTime());
    ZMQ.Socket client;
    ZMQ.Context ctx;
    boolean running = true;
    CountDownLatch latch;

    public void run() {
        ctx = ZMQ.context(1);
        client = ctx.socket(ZMQ.DEALER);

        //  Set random identity to make tracing easier
        String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
        client.setIdentity(identity.getBytes());
        client.connect(address);

        ZMQ.Poller poller = new ZMQ.Poller(1);
        poller.register(client, ZMQ.Poller.POLLIN);

        int requestNbr = 0;
        Set<String> msgOut = Collections.synchronizedSet(new HashSet<String>());
        while (running) {
            //  Tick once per second, pulling in arriving messages
            poller.poll(2000);
            if (poller.pollin(0)) {
                byte msg[] = client.recv(0);
                System.out.println(identity + " Client received "
                        + new String(msg));
                String compare = new String(msg);
                if(msgOut.contains(compare)){
                    latch.countDown();
                    msgOut.remove(compare);
                }

                if(latch.getCount()==0) break;

            }
            String out = String.format(identity+" request #%d", ++requestNbr);
            msgOut.add(out);
            System.out.println(identity + " Client sent "
                    + msgOut);
            if(handler!=null)
                client.send(handler,ZMQ.SNDMORE);
            client.send(out, 0);
        }
        client.close();
        ctx.term();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
