import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class ClasterForAdmins {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> {
                    final var options = new DeploymentOptions().setWorker(true);
                    for(int i = 0; i < 3; i++)
                        vertxResult.result().deployVerticle(new Admin(i, i + 1, (i+1) * 2 + 1), options);
                }
        );
    }
}
