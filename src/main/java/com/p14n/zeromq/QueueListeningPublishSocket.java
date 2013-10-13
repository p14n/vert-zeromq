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
                b = q.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
            if (b == null) continue;

            for(int index = 0;index<b.length;index++){
                push.send(b[index], index==b.length-1?0:ZMQ.SNDMORE);
            }
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
