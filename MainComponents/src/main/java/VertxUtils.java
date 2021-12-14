import io.vertx.core.Vertx;

import java.util.concurrent.ThreadLocalRandom;

public class VertxUtils {
    public static String listenerClans(String mapName, Vertx vertx){
        final String[] clans = new String[1];
        vertx.sharedData().getAsyncMap(mapName, map ->
                map.result().entries(item -> {
                    item.result().forEach((name, nameClan) ->{
                                if (ThreadLocalRandom.current().nextBoolean() && clans[0] == null){
                                    clans[0] = (String) name;
                                    if(clans[0] != null)
                                        return;
                                }
                            }
                    );
                })
        );
        return clans[0];
    }
}
