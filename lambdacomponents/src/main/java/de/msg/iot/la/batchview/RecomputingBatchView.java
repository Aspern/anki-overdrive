package de.msg.iot.la.batchview;


public abstract class RecomputingBatchView<D> extends AbstractBatchView<D> {

    private Listener listener = new Listener() {
    };

    public interface Listener {

        default void onUpdate() {
        }

    }

    @Override
    protected void update() {
        drop();
        recompute();
        listener.onUpdate();
    }

    public RecomputingBatchView onUpdate(Listener listener) {
        this.listener = listener;
        return this;
    }

    protected abstract void recompute();

}
