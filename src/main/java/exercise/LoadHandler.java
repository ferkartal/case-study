package exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadHandler {

    private static final int MAX_PRICE_UPDATES = 100;
    private static final int PERIOD = 1;
    private static final int INITIAL_DELAY = 1;

    private ConcurrentLinkedQueue<PriceUpdate> priceUpdates;
    private final ScheduledExecutorService scheduler;
    private final Consumer consumer;

    public LoadHandler(Consumer consumer) {
        this.consumer = consumer;
        this.scheduler = Executors.newScheduledThreadPool(1);
        priceUpdates = new ConcurrentLinkedQueue<>();
        schedulesTimer();
    }

    /*
    This method is responsible for adding to stock update into the queue
    In case of existing of the same company inside to queue, the method provides to remove the existing one
    and add new stock update
     */
    public void receive(PriceUpdate priceUpdate) {
        priceUpdates.remove(priceUpdate);
        priceUpdates.add(priceUpdate);
    }

    /*
    This method is called in every second and responsible for delivering stock updates to consumer.
    toBeConsumedMessages is a list of stock updates which will be sent to consumer within 1 second
    toBeConsumedMessages will be populated based on value of MAX_PRICE_UPDATES and priceUpdates
    Remove operation performs to prevent increasing the size of list with old stock updates and to sent the most recent one.
     */
    private void schedulesTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            List<PriceUpdate> toBeConsumedMessages = new ArrayList<>(MAX_PRICE_UPDATES);
            PriceUpdate p;

            while (toBeConsumedMessages.size() < MAX_PRICE_UPDATES && (p = priceUpdates.poll()) != null) {
                toBeConsumedMessages.remove(p);
                toBeConsumedMessages.add(p);
            }

            if (!toBeConsumedMessages.isEmpty()) {
                consumer.send(toBeConsumedMessages);
            }

        }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }
}
