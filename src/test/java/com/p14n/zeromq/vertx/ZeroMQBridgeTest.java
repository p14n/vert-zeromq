package com.p14n.zeromq.vertx;

import com.p14n.zeromq.*;
import org.junit.*;

import java.util.concurrent.*;

/**
 * Created by Dean Pehrsson-Chapman
 * Date: 10/10/2013
 */
@Ignore("No longer works as the verticle needs to be a worker - needs rewriting")
public class ZeroMQBridgeTest {

    static int clients = 100;
    static int requests = 2500;
    static int firstBurstMins = 60;
    static int breakPeriodMins = 240;
    static int secondBurstMins = 60;

    public static void main(String ars[]) throws InterruptedException {
        if(ars.length==2){
            clients = Integer.parseInt(ars[0]);
            requests = Integer.parseInt(ars[1]);
        }
        EchoSocket echo = startEchoSocket("tcp://localhost:5558","echoHandler");
        System.out.println(String.format("Testing with %s clients and %s requests",clients,requests));
        long start = System.currentTimeMillis();

        long breakTime = start+(firstBurstMins*60000);
        long secondBurst = breakTime+(breakPeriodMins*60000);
        long end = secondBurst + (secondBurstMins * 60000);

        System.out.println("Start first burst");
        while(System.currentTimeMillis()<breakTime){
            testEchoSocket();
        }
        while(System.currentTimeMillis()<secondBurst){
            System.out.println("Break period, start again in "+((secondBurst-System.currentTimeMillis())/60000));
            Thread.sleep(300000);
        }
        System.out.println("Start second burst");
        while(System.currentTimeMillis()<end){
            testEchoSocket();
        }
        echo.setListening(false);
    }

    private static EchoSocket startEchoSocket(String address,String name) throws InterruptedException {
        EchoSocket echo = new EchoSocket(address,name);
        new Thread(echo).start();
        Thread.sleep(100);
        return echo;
    }
    public static void testEchoSocket() throws InterruptedException {
        new ZeroMQBridgeTest().shouldGetMessagesBackFromAVertHandler("echoHandler");
    }

    public static void testEchoEventBus() throws InterruptedException {
        new ZeroMQBridgeTest().shouldGetMessagesBackFromAVertHandler("echo");
    }

    private TestClient createAndStartClient(String name){
        TestClient client = new TestClient("tcp://localhost:5558", requests);
        client.setHandler(name);
        new Thread(client).start();
        return client;
    }

    private TestClient[] createClients(int count,String name){
        TestClient[] clis = new TestClient[count];
        for(int i=0;i<count;i++){
            clis[i] = createAndStartClient(name);
        }
        return clis;
    }

    public void shouldGetMessagesBackFromAVertHandler(final String name) throws InterruptedException {

        final TestClient[] cls = createClients(clients,name);

        final long start = System.currentTimeMillis();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(TestClient c:cls)
                        c.waitFor(((clients*requests)/300)+1);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(name+" took "+(System.currentTimeMillis()-start));

            }
        });
        t.start();
        t.join();
    }


}