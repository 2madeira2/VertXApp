import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class ClasterMainForUsers {

    public static void main(String[] args)
    {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> {
                    final var vertx = vertxResult.result();
                    final var options = new DeploymentOptions().setWorker(true);
                    final var factory = new User.Factory();
                    vertx.registerVerticleFactory(factory);
                    final DeploymentOptions optionsMember = new DeploymentOptions().setWorker(true).setInstances(10);
                    vertx.deployVerticle(
                            factory.prefix() + ':' + User.class.getName(),
                            optionsMember,
                            res -> System.out.println("Members deploy result: " + res.succeeded())
                    );
                }
        );
    }
}
