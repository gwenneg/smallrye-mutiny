package io.smallrye.mutiny.streams.stages;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.After;
import org.junit.Test;

import io.smallrye.mutiny.Multi;

/**
 * Checks the behavior of the {@link OnErrorResumeStageFactory}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class OnErrorResumeWithStageFactoryTest extends StageTestBase {

    private final OnErrorResumeStageFactory factory = new OnErrorResumeStageFactory();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @After
    public void cleanup() {
        executor.shutdown();
    }

    @Test
    public void create() throws ExecutionException, InterruptedException {
        Multi<Integer> publisher = Multi.createFrom().items(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .emitOn(executor);

        List<Integer> list = ReactiveStreams.<Integer> failed(new Exception("BOOM"))
                .onErrorResumeWith(t -> ReactiveStreams.fromPublisher(publisher))
                .toList()
                .run().toCompletableFuture().exceptionally(x -> Collections.emptyList()).get();

        assertThat(list).hasSize(10);
    }

    @Test
    public void createAndFailAgain() throws ExecutionException, InterruptedException {
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<Integer> list = ReactiveStreams.<Integer> failed(new RuntimeException("BOOM"))
                .onErrorResumeWith(t -> ReactiveStreams.failed(new RuntimeException("Failed")))
                .toList()
                .run().toCompletableFuture().exceptionally(x -> {
                    error.set(x);
                    return Collections.emptyList();
                }).get();

        assertThat(list).hasSize(0);
        assertThat(error.get()).hasMessage("Failed");
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutStage() {
        factory.create(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createWithoutFunction() {
        factory.create(null, () -> null);
    }

}
