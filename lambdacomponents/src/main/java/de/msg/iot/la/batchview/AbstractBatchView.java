package de.msg.iot.la.batchview;


public abstract class AbstractBatchView<D> implements BatchView<D> {

    private volatile boolean running = false;

    @Override
    public void run() {
        running = true;
        try {
            while (running) {
                update();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected abstract void handleException(Exception e);

    protected abstract void update() throws Exception;

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }


}
