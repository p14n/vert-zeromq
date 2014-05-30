import org.vertx.java.core.*;
import org.vertx.java.core.eventbus.*;
import org.vertx.java.platform.*;

public class Server extends Verticle {

    public void start() {
        container.deployModule("p14n~vert-zeromq~1.0.0", container.config(),1, new AsyncResultHandler<String>() {
            public void handle(AsyncResult<String> deployResult) {
                if (deployResult.succeeded()) {
                    container.logger().info("Deploy succeeded");
                } else {
                    container.logger().info("Deploy failed");
                    deployResult.cause().printStackTrace();
                }
            }
        });
        vertx.eventBus().registerHandler("echo", new Handler<Message<byte[]>>() {
            @Override
            public void handle(Message<byte[]> message) {
                message.reply(message.body());
            }
        });
    }
}
