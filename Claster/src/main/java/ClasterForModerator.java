import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class ClasterForModerator {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> {
                    final var options = new DeploymentOptions().setWorker(true);

                    for(int i = 0; i < 5; i++)
                        vertxResult.result().deployVerticle(new Moderator(i), options);
                }
        );
    }
}
