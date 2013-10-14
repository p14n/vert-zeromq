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

        ZMQ.Socket pull = c.socket(ZMQ.PULL);
        pull.connect(backendAddress);

        ZMQ.Poller poller = new ZMQ.Poller(2);
        poller.register(server, ZMQ.Poller.POLLIN);
        poller.register(pull, ZMQ.Poller.POLLIN);

        byte[][] results = new byte[2][];

        while (running) {
            poller.poll();
            if (poller.pollin(0)) {

                int resultIndex = 0;

                // receive message
                results[resultIndex++] = server.recv(0);
                while(server.hasReceiveMore()){

                    if(resultIndex==results.length){
                        byte[][] tmp = new byte[resultIndex+1][];
                        System.arraycopy(results,0,tmp,0,resultIndex);
                        results = tmp;
                    }

                    byte[] resultBytes = server.recv(0);
                    results[resultIndex++] = resultBytes;
                }

                byte[][] copy = new byte[resultIndex][];
                System.arraycopy(results,0,copy,0,resultIndex);

                // Broker it
                handleBlockingRequest(copy, new MessageResponder(copy[0], q));

            }

            if (poller.pollin(1)) {
                // receive message
                byte[] id = pull.recv(0);
                byte[] msg = pull.recv(0);

                // Broker it
                server.send(id, ZMQ.SNDMORE);
                server.send(msg, 0);
            }
        }

        server.close();
        pull.close();

    }

    protected abstract void handleBlockingRequest(byte[][] msg, MessageResponder messageResponder);

    public void setRunning(boolean running) {
        this.running = running;
    }
}
