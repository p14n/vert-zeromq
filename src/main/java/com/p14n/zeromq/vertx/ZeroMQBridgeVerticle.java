package com.p14n.zeromq.vertx;

import org.vertx.java.core.logging.*;
import org.vertx.java.platform.*;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 27/10/2013
 */
public class ZeroMQBridgeVerticle extends Verticle {

    ZeroMQBridge bridge;

    @Override
    public void start() {

        String address = getContainer().config().getString("address");
        long timeout  = getContainer().config().getLong("timeout",5000);
        final Logger log = getContainer().logger();
        getContainer().logger().info("Starting ZeroMQBridge on " + address);

        if (address == null || address.length() == 0)
            throw new IllegalArgumentException("No address specified");

        bridge = new ZeroMQBridge(address, vertx, timeout) {
            @Override
            protected void error(String s, Throwable cause) {
                log.error(s, cause);
            }

            @Override
            protected void info(String s) {
                log.info(s);
            }
        };

        try {
            bridge.start();
            getContainer().logger().info("ZeroMQBridge started on " + address);
        } catch (Exception e) {
            getContainer().logger().error("Failed to start ZeroMQBridge", e);
        }
    }

    @Override
    public void stop() {
        if (bridge != null)
            bridge.stop();
    }
}
