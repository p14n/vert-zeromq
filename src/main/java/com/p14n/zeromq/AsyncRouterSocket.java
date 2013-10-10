package com.p14n.zeromq;

import org.zeromq.ZMQ;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public abstract class AsyncRouterSocket implements Runnable {

    public AsyncRouterSocket(ZMQ.Context c,BlockingQueue<byte[][]> q, String frontendAddress, String backendAddress) {
        this.q = q;
        this.frontendAddress = frontendAddress;
        this.backendAddress = backendAddress;
        this.c = c;
    }

    BlockingQueue<byte[][]> q;
    String frontendAddress;
    String backendAddress;
    boolean running = true;
    ZMQ.Context c;

    @Override
    public void run() {

        ZMQ.Socket server = c.socket(ZMQ.ROUTER);
        server.bind(frontendAddress);

        ZMQ.Socket sub = c.socket(ZMQ.SUB);
        sub.connect(backendAddress);
        sub.subscribe("".getBytes());

        ZMQ.Poller poller = new ZMQ.Poller(2);
        poller.register(server, ZMQ.Poller.POLLIN);
        poller.register(sub, ZMQ.Poller.POLLIN);

        while (running) {
            poller.poll();
            if (poller.pollin(0)) {

                // receive message
                byte[] id = server.recv(0);
                byte[] msg = server.recv(0);
                byte[] handler = null;
                if(server.hasReceiveMore()){
                    handler = msg;
                    msg = server.recv(0);
                }

                // Broker it
                System.out.println("Server received request " + new String(msg)
                        + " id " + new String(id));
                handleBlockingRequest(handler,msg, new MessageResponder(id, q));

            }

            if (poller.pollin(1)) {
                // receive message
                byte[] id = sub.recv(0);
                byte[] msg = sub.recv(0);

                // Broker it
                System.out.println("Server received response " + new String(msg)
                        + " id " + new String(id));
                server.send(id, ZMQ.SNDMORE);
                server.send(msg, 0);
            }
        }

        server.close();
        sub.close();

    }

    protected abstract void handleBlockingRequest(byte[] handler, byte[] msg, MessageResponder messageResponder);

    public void setRunning(boolean running) {
        this.running = running;
    }
}
