package de.msg.iot.anki.elasticplayground.profit;


import de.msg.iot.anki.elasticplayground.entity.OptimalSpeed;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProfitPipeline implements Runnable {

    private final ExecutorService threadPool;
    private final EntityManager mysql;


    public ProfitPipeline() {
        this.threadPool = Executors.newCachedThreadPool();
        this.mysql = Persistence.createEntityManagerFactory("anki")
                .createEntityManager();
    }

    @Override
    public void run() {
        try {
            Future<Boolean> task = threadPool.submit(new PublishQualityData());

            task.get();

            Future<List<String>> distinctPositions = threadPool.submit(new FindDistinctPositions());

            mysql.getTransaction().begin();
            mysql.createNativeQuery("truncate table OPTIMALSPEED ")
                    .executeUpdate();
            mysql.getTransaction().commit();

            List<Future<OptimalSpeed>> futures = distinctPositions.get()
                    .stream()
                    .map(position -> threadPool.submit(new MachineLearningWorker(position, 0.85)))
                    .collect(Collectors.toList());

            for (Future<OptimalSpeed> future : futures) {
                OptimalSpeed optimalSpeed = future.get();
                System.out.print("Found optimal Speed for " + optimalSpeed.getPosition() + ", " + optimalSpeed.getSpeed());
                mysql.getTransaction().begin();
                mysql.persist(optimalSpeed);
                mysql.getTransaction().commit();
            }

            threadPool.shutdown();
            threadPool.awaitTermination(10, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        final ExecutorService thread = Executors.newSingleThreadExecutor();
        thread.submit(new ProfitPipeline());


        thread.shutdown();
        thread.awaitTermination(10, TimeUnit.MINUTES);
    }
}
