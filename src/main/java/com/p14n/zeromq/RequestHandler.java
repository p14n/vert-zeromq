package com.p14n.zeromq;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 13/10/2013
 */
public interface RequestHandler {

    void handleRequest(byte[][] message,MessageResponder responder);

}
