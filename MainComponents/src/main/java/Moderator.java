import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.JavaVerticleFactory;

import java.util.concurrent.ThreadLocalRandom;

public class Moderator extends AbstractVerticle {

    private String _name;
    private String _clan;
    private int _max = 20;
    private MessageConsumer _ms, _msExit;
    private long _timerId;
    private String _tempString;

    public Moderator(int n)
    {
        _name = "Moderator" + n;
    }

    public void start()
    {
       setPeriodicForMessageToClan();
    }

    private void createMSForExitAndReceiveUsers()
    {
        _ms = vertx.eventBus().consumer(_clan, event -> {
                    if (ThreadLocalRandom.current().nextBoolean() ) {
                        vertx.sharedData().getCounter(_clan + "CountUser", counter -> {
                            if (counter.succeeded()) {
                                counter.result().get(event1 -> {
                                    if (event1.result() < _max) {
                                        event.reply("We accepted you");
                                        counter.result().incrementAndGet(number -> System.out.println(_name + " accepted " + event.body() + " in " + _clan + " (" + number.result()));
                                    }
                                }) ;
                            }
                        });
                    }
                }
        );
        _msExit = vertx.eventBus().consumer(_clan + "Exit", event->{
            _ms.pause();
            _msExit.pause();
            setPeriodicForMessageToClan();
        });
    }

    private void setPeriodicForMessageToClan()
    {
        _timerId = vertx.setPeriodic(5000, timer ->
                {
                    if(_clan == null)
                        vertx.<String>executeBlocking(promise -> promise.complete(
                                randomChooseClan("clans")), res -> messageToClan(res.result()
                        ));
                }
        );
    }

    private boolean messageToClan(String clan)
    {
        if(clan == null )
            return false;
        final DeliveryOptions options = new DeliveryOptions().setSendTimeout(1000);
        System.out.println(_name + " want to "+ clan);
        vertx.eventBus().request(clan + "Moderators", _name, options, reply -> {
            if (reply.succeeded()) {
                _clan = clan;
                vertx.cancelTimer(_timerId);
                createMSForExitAndReceiveUsers();
                //System.out.println(reply.result().body().toString());
                _max = Integer.parseInt(reply.result().body().toString());
                System.out.println(_name + " was accepted in " + clan);
            }
        });
        return _clan == null ? false : true;
    }

    public String randomChooseClan(String mapName){
        vertx.sharedData().getAsyncMap(mapName, map ->
                map.result().entries(item -> {
                    item.result().forEach((name, nameClan) ->{
                                if (ThreadLocalRandom.current().nextBoolean()){
                                    _tempString = (String) name;
                                    if(_tempString != null)
                                        return;
                                }
                            }
                    );
                })
        );
        return _tempString;
    }

    public static final class ModeratorFactory extends JavaVerticleFactory {
        private static int number;

        @Override
        public String prefix() {
            return "sphere";
        }

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
            return new Moderator(number++);
        }
    }
}
