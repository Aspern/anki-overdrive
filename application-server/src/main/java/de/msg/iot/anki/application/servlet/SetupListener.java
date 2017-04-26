package de.msg.iot.anki.application.servlet;


import de.msg.iot.anki.application.entity.Setup;
import de.msg.iot.anki.application.kafka.SetupKafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SetupListener implements ServletContextListener {

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private final Logger logger = LogManager.getLogger(SetupListener.class);
    private final EntityManager manager = Persistence
            .createEntityManagerFactory("anki")
            .createEntityManager();
    private final SetupKafkaConsumer consumer = new SetupKafkaConsumer();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("Starting consumer for AnkiOverdriveSetupListener...");
        consumer.handle(setup -> {
            try {
                if (setup.isOnline()) {
                    if (manager.createQuery("select d from Setup d where d.ean = '" + setup.getEan() + "'")
                            .getResultList()
                            .isEmpty()) {
                        manager.getTransaction().begin();
                        manager.persist(setup);
                        manager.getTransaction().commit();
                        logger.info("Stored setup with uuid [" + setup.getEan() + "].");
                    } else {
                        logger.warn("Setup with uuid [" + setup.getEan() + "] already exists!");
                    }
                } else {

                    ((List<Setup>) manager.createQuery("select d from Setup d where d.ean = '" + setup.getEan() + "'")
                            .getResultList()).forEach(record -> {
                        manager.getTransaction().begin();
                        manager.remove(record);
                        manager.getTransaction().commit();
                        logger.info("Deleted setup with uuid [" + record.getEan() + "].");
                    });
                }

            } catch (Exception e) {
                logger.error(e);
            }
        });


        threadPool.submit(consumer);

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.info("Shutting down consumer for AnkiOverdriveSetupListener...");
        try {
            consumer.stop();
            threadPool.shutdown();
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e);
        }
    }
}
