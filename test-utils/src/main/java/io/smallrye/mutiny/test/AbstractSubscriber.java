package io.smallrye.mutiny.test;

import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@SuppressWarnings("SubscriberImplementation")
public class AbstractSubscriber<T> implements Subscriber<T>, Subscription {

    private final long upfrontRequest;

    public AbstractSubscriber() {
        upfrontRequest = 0;
    }

    public AbstractSubscriber(long req) {
        upfrontRequest = req;
    }

    private AtomicReference<Subscription> upstream = new AtomicReference<>();

    @Override
    public void onSubscribe(Subscription s) {
        if (upstream.compareAndSet(null, s)) {
            if (upfrontRequest > 0) {
                s.request(upfrontRequest);
            }
        } else {
            throw new IllegalStateException("We already have a subscription");
        }
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void request(long n) {
        Subscription subscription = upstream.get();
        if (subscription != null) {
            subscription.request(n);
        } else {
            throw new IllegalStateException("No subscription");
        }
    }

    @Override
    public void cancel() {
        Subscription subscription = upstream.get();
        if (subscription != null) {
            subscription.cancel();
        } else {
            throw new IllegalStateException("No subscription");
        }
    }
}
