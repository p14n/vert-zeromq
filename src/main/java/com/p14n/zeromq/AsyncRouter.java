package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class AsyncRouter {

   // ExecutorService service;
    QueueListeningPublishSocket back;
    AsyncRouterSocket front;
    BlockingQueue<byte[][]> queue;
    ZMQ.Context c;
    String address;
    RequestHandler handler;

    public AsyncRouter setRequestHandler(RequestHandler handler) {
        this.handler = handler;
        return this;
    }

    public AsyncRouter(String address) {
        this.address = address;
        queue = new LinkedBlockingQueue<byte[][]>();
    }

    public AsyncRouter start() {

        c = ZMQ.context(2);

        front = new AsyncRouterSocket(c, queue, address, "inproc://zmq-async-backend") {
            @Override
            protected void handleBlockingRequest(byte[][] msg, MessageResponder messageResponder) {
                if(handler!=null)
                    handler.handleRequest(msg,messageResponder);
            }
        };
        back = new QueueListeningPublishSocket(c, queue, "inproc://zmq-async-backend");
        run(back);
        run(front);
        return this;
    }

    public AsyncRouter stop() {
        if(back!=null)
            back.setRunning(false);
        if(front!=null)
            front.setRunning(false);
        if(queue!=null)
            queue.clear();
        if(c!=null)
            c.term();
        return this;
    }

    protected void run(Runnable runnable){
        new Thread(runnable).start();
    }
    protected void error(String s, Throwable cause) {
        System.err.println(s);
        if(cause!=null) cause.printStackTrace();
    }

    protected void info(String s) {
        System.out.println(s);
    }


}