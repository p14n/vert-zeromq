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

    private BlockingQueue<byte[][]> q;
    private String frontendAddress;
    private String backendAddress;
    boolean running = true;
    private ZMQ.Context c;
    private byte[][] results = new byte[2][];

    @Override
    public void run() {

        ZMQ.Socket server = createFrontEndSocket(c,frontendAddress);

        ZMQ.Socket pull = c.socket(ZMQ.PULL);
        pull.connect(backendAddress);

        ZMQ.Poller poller = new ZMQ.Poller(2);
        poller.register(server, ZMQ.Poller.POLLIN);
        poller.register(pull, ZMQ.Poller.POLLIN);

        while (running) {
            poller.poll();
            if (poller.pollin(0)) {

                byte[][] msg = fromSocket(server);

                // Broker it
                handleBlockingRequest(msg, new MessageResponder(msg[0], q));

            }

            if (poller.pollin(1)) {
                // receive message
                byte[][] msg = fromSocket(pull);

                // Broker it
                for(int i = 0 ;i<msg.length;i++){
                    if(i==msg.length-1){
                        server.send(msg[i], 0);
                    } else {
                        server.send(msg[i], ZMQ.SNDMORE);
                    }
                }
            }
        }

        server.close();
        pull.close();

    }

    protected ZMQ.Socket createFrontEndSocket(ZMQ.Context c,String frontendAddress) {
        ZMQ.Socket server = c.socket(ZMQ.ROUTER);
        server.bind(frontendAddress);
        return server;
    }

    private byte[][] fromSocket(ZMQ.Socket server) {
        int resultIndex = 0;

        // receive message
        resultIndex = receivemessage(server, resultIndex);

        byte[][] copy = new byte[resultIndex][];
        System.arraycopy(results,0,copy,0,resultIndex);
        return copy;
    }

    private int receivemessage(ZMQ.Socket server, int resultIndex) {
        results[resultIndex++] = server.recv(0);
        while(server.hasReceiveMore()){

            checkBufferSize(resultIndex);
            byte[] resultBytes = server.recv(0);
            results[resultIndex++] = resultBytes;
        }
        return resultIndex;
    }

    private void checkBufferSize(int resultIndex) {
        if(resultIndex==results.length){
            byte[][] tmp = new byte[resultIndex+1][];
            System.arraycopy(results,0,tmp,0,resultIndex);
            results = tmp;
        }
    }

    protected abstract void handleBlockingRequest(byte[][] msg, MessageResponder messageResponder);

    public void setRunning(boolean running) {
        this.running = running;
    }
}
