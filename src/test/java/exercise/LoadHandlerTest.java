package exercise;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoadHandlerTest {

    @InjectMocks
    private LoadHandler loadHandler;

    @Mock
    private Consumer consumer;

    @Test
    public void it_should_send_stock_updates_to_consumer_based_on_the_capacity() throws InterruptedException {
        //Given
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 101; i++) {
            int finalI = i;
            executor.execute(() -> loadHandler.receive(new PriceUpdate("Apple" + finalI, 97.85)));
        }

        //When
        executor.awaitTermination(3, TimeUnit.SECONDS);

        //Then
        ArgumentCaptor<List<PriceUpdate>> captor = ArgumentCaptor.forClass(List.class);
        verify(consumer, times(2)).send(captor.capture());

        List<List<PriceUpdate>> priceUpdates = captor.getAllValues();
        assertThat(priceUpdates.get(0)).hasSize(100);
        assertThat(priceUpdates.get(1)).hasSize(1);
    }

    @Test
    public void it_should_remove_old_stock_updates_while_sending_to_consumer() throws InterruptedException {

        //Given
        ExecutorService executor = Executors.newFixedThreadPool(25);

        PriceUpdate apple = new PriceUpdate("Apple", 97.85);
        PriceUpdate googleOld = new PriceUpdate("Google", 160.71);
        PriceUpdate facebook = new PriceUpdate("Facebook", 91.66);
        PriceUpdate googleNew = new PriceUpdate("Google", 160.73);


        executor.execute(() -> {
            loadHandler.receive(apple);
            loadHandler.receive(googleOld);
            loadHandler.receive(facebook);
            loadHandler.receive(googleNew);

        });

        //When
        executor.awaitTermination(2, TimeUnit.SECONDS);

        //Then
        ArgumentCaptor<List<PriceUpdate>> captor = ArgumentCaptor.forClass(List.class);
        verify(consumer, times(1)).send(captor.capture());

        List<PriceUpdate> priceUpdates = captor.getValue();
        assertThat(priceUpdates).hasSize(3);
        assertThat(priceUpdates).containsExactlyInAnyOrder(apple, googleNew, facebook);
    }
}