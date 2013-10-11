package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class QueueListeningPublishSocket implements Runnable {

    BlockingQueue<byte[][]> q;
    String address;
    boolean running = true;
    ZMQ.Context c;

    public QueueListeningPublishSocket(ZMQ.Context c, BlockingQueue<byte[][]> q, String address) {
        this.q = q;
        this.address = address;
        this.c = c;
    }

    @Override
    public void run() {

        ZMQ.Socket push = c.socket(ZMQ.PUSH);
        configure(push);
        push.bind(address);

        while (running) {
            byte[][] b = null;
            try {
                b = q.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
            if (b == null) continue;

            byte[] id = b[0];
            byte[] msg = b[1];
            System.out.println("Server responder received " + new String(msg)
                    + " id " + new String(b[0]));
            push.send(id, ZMQ.SNDMORE);
            push.send(msg, 0);

        }

        push.close();

    }

    protected void configure(ZMQ.Socket pub) {
        pub.setLinger(5000);
        pub.setSndHWM(0);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
