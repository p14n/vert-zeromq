package com.p14n.zeromq;

import java.util.Queue;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
public class MessageResponder {

    final byte[] id;
    final Queue<byte[][]> q;

    public MessageResponder(byte[] id, Queue<byte[][]> q) {
        this.id = id;
        this.q = q;
    }

    public byte[] getSocketId() {
        return id;
    }

    public void respond(byte[] msg){
        q.add(new byte[][]{id,msg});
    }

    public void respond(byte[] msg, byte[] address) {
        q.add(new byte[][]{id,msg,address});
    }
}
