package de.msg.iot.anki.productimprovement;

import de.msg.iot.anki.productimprovement.ml.MLMaster;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProductImprovementApplication {

    private final ScheduledExecutorService runner = Executors.newScheduledThreadPool(1);
    private final long periodInMinutes;
    private final Logger logger = Logger.getLogger(ProductImprovementApplication.class);
    private volatile boolean running = false;

    public ProductImprovementApplication(long timeout) {
        this.periodInMinutes = timeout;
    }

    public ProductImprovementApplication() {
        this(1);
    }

    public void start() {

        if (!running) {
            logger.info("Starting " + getClass().getSimpleName() + ".");
            this.runner.scheduleAtFixedRate(new MLMaster(), 0, periodInMinutes, TimeUnit.MINUTES);
            this.running = true;
        } else {
            logger.warn(getClass().getSimpleName() + " is still running.");
        }
    }

    public void stop() {
        if (running) {
            try {
                logger.info("Shutting down " + getClass().getSimpleName() + ".");
                runner.shutdown();
                runner.awaitTermination(30, TimeUnit.MINUTES);
                running = false;
                logger.info("Finished " + getClass().getSimpleName() + ".");
            } catch (Exception e) {
                logger.error("Cannot shutdown runner.", e);
            }
        } else {
            logger.warn(getClass().getSimpleName() + " is not running.");
        }
    }

    public static void main(String[] args) throws Exception {
        ProductImprovementApplication application = new ProductImprovementApplication();
        application.start();
        Thread.sleep(10000);
        application.stop();
    }
}
