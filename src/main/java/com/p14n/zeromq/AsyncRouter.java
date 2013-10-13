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
public class AsyncRouter {

    ExecutorService service;
    QueueListeningPublishSocket back;
    AsyncRouterSocket front;
    BlockingQueue<byte[][]> queue;
    ZMQ.Context c;
    String address;
    RequestHandler handler;

    public AsyncRouter handleRequest(RequestHandler handler) {
        this.handler = handler;
        return this;
    }

    public AsyncRouter(String address) {
        this.address = address;
        queue = new LinkedBlockingQueue<byte[][]>();
    }

    public AsyncRouter start() {
        c = ZMQ.context(2);
        service = Executors.newFixedThreadPool(2);
        front = new AsyncRouterSocket(c, queue, address, "inproc://zmq-async-backend") {
            @Override
            protected void handleBlockingRequest(byte[][] msg, MessageResponder messageResponder) {
                if(handler!=null)
                    handler.handleRequest(msg,messageResponder);
            }
        };
        back = new QueueListeningPublishSocket(c, queue, "inproc://zmq-async-backend");
        service.submit(back);
        service.submit(front);
        return this;
    }

    public AsyncRouter stop() {
        back.setRunning(false);
        front.setRunning(false);
        service.shutdown();
        queue.clear();
        c.term();
        return this;
    }

}