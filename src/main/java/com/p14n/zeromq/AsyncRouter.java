package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public abstract class AsyncRouter {

    ExecutorService service;
    QueueListeningPublishSocket back;
    AsyncRouterSocket front;
    BlockingQueue<byte[][]> queue;
    ZMQ.Context c;
    String address;

    public AsyncRouter(String address) {
        this.address = address;
        queue = new LinkedBlockingQueue<byte[][]>();
    }

    public void start() {
        c = ZMQ.context(2);
        service = Executors.newFixedThreadPool(2);
        front = new AsyncRouterSocket(c, queue, address, "inproc://backend") {
            @Override
            protected void handleBlockingRequest(byte[] handler, byte[] msg, MessageResponder messageResponder) {
                AsyncRouter.this.handleBlockingRequest(handler, msg, messageResponder);
            }
        };
        back = new QueueListeningPublishSocket(c, queue, "inproc://backend");
        service.submit(back);
        service.submit(front);
    }

    public void stop() {
        back.setRunning(false);
        front.setRunning(false);
        service.shutdown();
        queue.clear();
        c.term();
    }

    protected abstract void handleBlockingRequest(byte[] handler, byte[] msg, MessageResponder messageResponder);

}