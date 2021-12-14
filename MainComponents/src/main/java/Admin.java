import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.impl.JavaVerticleFactory;

import java.util.concurrent.ThreadLocalRandom;

public class Admin extends AbstractVerticle {
    private String _name;
    private String _clan;
    private int _maxCountModerator;
    private int _maxCountUsers;

    public Admin(int n, int maxCountModerator, int maxCountUsers)
    {
        _clan = "Clan" + n;
        _name = "Admin" + n;
        _maxCountModerator = maxCountModerator;
        _maxCountUsers = maxCountUsers;
    }

    public void start()
    {
        vertx.setPeriodic(10000, timer ->
                vertx.sharedData().getAsyncMap("clans", map ->
                        map.result().put(_clan, _clan, completion ->{
                            checkCountMembers();
                            System.out.println(_clan + " is alive");
                                }
                        ))
        );
        vertx.eventBus().consumer(_clan + "Moderators", event -> {
            if (ThreadLocalRandom.current().nextBoolean())
            {
                vertx.sharedData().getCounter(_clan + "Moderators", counter -> {
                    if(counter.succeeded())
                    {
                        counter.result().get(number ->{
                            if(number.result() < _maxCountModerator)
                            {
                                event.reply(_maxCountUsers + "");
                                counter.result().getAndIncrement(number2 ->{});
                            }
                        });
                    }
                });
            }
        });

    }

    public void sendMessageExit()
    {
        System.out.println(_clan + " : all Hana , buy");
        vertx.eventBus().publish(_clan + "Exit", null);
    }

    private void checkCountMembers()
    {
        vertx.sharedData().getCounter(_clan + "CountUser", counter -> {
            if(counter.succeeded())
            {
                counter.result().get(number ->{
                    if(number.result() > _maxCountUsers)
                    {
                        System.out.print(number.result());
                        sendMessageExit();
                    }
                });
            }
        });
    }

    public static final class AdminFactory extends JavaVerticleFactory {
        private int number;

        @Override
        public String prefix() {
            return "sphere";
        }

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        public Verticle createVerticle(String verticleName, ClassLoader classLoader) {
            return new Admin(number++, 10,10);
        }
    }
}
