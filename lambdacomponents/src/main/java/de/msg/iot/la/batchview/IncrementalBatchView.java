package de.msg.iot.la.batchview;

import java.util.Collection;

public abstract class IncrementalBatchView<D> extends AbstractBatchView<D> {

    private Listener listener = new Listener() {
    };

    public interface Listener<D> {

        default void onUpdate(Collection<D> increment) {
        }

    }

    @Override
    protected void update() throws Exception {
        Collection<D> increment = compute();
        addIncrement(increment);
        listener.onUpdate(increment);
    }

    public IncrementalBatchView onUpdate(Listener listener) {
        this.listener = listener;
        return this;
    }


    protected abstract Collection<D> compute() throws Exception;

    protected abstract void addIncrement(Collection<D> increment);

}
