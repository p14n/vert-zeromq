package com.p14n.zeromq.vertx;

import org.vertx.java.platform.Verticle;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 27/10/2013
 */
public class ZeroMQBridgeVerticle extends Verticle {

    ZeroMQBridge bridge;

    @Override
    public void start() {
        String address = getContainer().config().getString("address");
        if (address == null || address.length() == 0)
            throw new IllegalArgumentException("No address specified");
        bridge = new ZeroMQBridge(address, vertx){
            @Override
            protected void error(String s, Throwable cause) {
                getContainer().logger().error(s,cause);
            }

            @Override
            protected void info(String s) {
                getContainer().logger().info(s);
            }
        };
        bridge.start();
    }

    @Override
    public void stop() {
        if (bridge != null)
            bridge.stop();
    }
}
