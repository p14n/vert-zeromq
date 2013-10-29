package com.p14n.zeromq.vertx;

import org.junit.Test;
import org.vertx.java.testframework.TestBase;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 29/10/2013
 */
public class ZeroMQBridgeVerticleTest extends TestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startApp(ZeroMQBridgeVerticleTestDeployer.class.getName());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void testZeroMQVerticle() throws Exception {
        startTest(getMethodName());
    }
}
