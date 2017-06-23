package de.msg.iot.anki.productimprovement.preprocess;


import de.msg.iot.anki.productimprovement.entity.AccelerationCommand;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScorePreProcessor {

    private final Client elastic;
    private final ExecutorService thread = Executors.newSingleThreadExecutor();
    private final EntityManager mysql = Persistence.createEntityManagerFactory("anki")
            .createEntityManager();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
    private final Logger logger = Logger.getLogger(ScorePreProcessor.class);

    private volatile boolean running = false;

    public ScorePreProcessor() {
        try {
            this.elastic = new

                    PreBuiltTransportClient(org.elasticsearch.common.settings.Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName("localhost"),
                            9300
                    ));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public void start() {
        if (!running) {
            logger.info("Starting " + getClass().getSimpleName() + ".");
            thread.submit(() -> {
                fetchFromMasterDataSet(hit -> {
                    storeToBatchView(
                            transformToEntity(hit)
                    );
                });
            });
            running = true;
        } else {
            logger.warn(getClass().getSimpleName() + " is already running!");
        }

    }

    public void awaitTerminationOr(Consumer<Exception> consumer) {
        try {
            logger.info("Shutting down " + getClass().getSimpleName() + ".");
            thread.shutdown();
            thread.awaitTermination(1, TimeUnit.MINUTES);
            running = false;
        } catch (InterruptedException e) {
            consumer.accept(e);
        } finally {
            if (!thread.isTerminated())
                consumer.accept(new Exception("Task is not terminated but reached Timeout."));
            else
                logger.info(getClass().getSimpleName() + " finished.");
        }
    }

    private void fetchFromMasterDataSet(Consumer<SearchHit> consumer) {
        SearchResponse scroll = elastic.prepareSearch("anki")
                .setTypes("accelCommand")
                .addSort("timestamp", SortOrder.ASC)
                .setSize(1000)
                .setQuery(
                        QueryBuilders.rangeQuery("score")
                                .gt(0)
                                .lte(0.05))
                .get();

        do {

            for (SearchHit hit : scroll.getHits().getHits())
                consumer.accept(hit);

            scroll = elastic.prepareSearchScroll(scroll.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();

        } while (scroll.getHits().getHits().length != 0);
    }

    private AccelerationCommand transformToEntity(SearchHit hit) {
        AccelerationCommand command = new AccelerationCommand();

        try {
            Map<String, Object> source = hit.getSource();

            command.setAi_1((int) source.get("ai_1"));
            command.setP1((String) source.get("p1"));
            command.setP2((String) source.get("p2"));
            command.setScore((double) source.get("score"));
            command.setTimestamp(dateFormat.parse((String) source.get("timestamp")));
            command.setV0((int) source.get("v0"));
            command.setVi((int) source.get("vi"));
            command.setVi_1((int) source.get("vi_1"));

        } catch (Exception e) {
            logger.error("Cannot transform command.", e);
        }

        return command;
    }

    private void storeToBatchView(AccelerationCommand command) {
        try {
            mysql.getTransaction().begin();
            mysql.persist(command);
            mysql.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Cannot store command in mysql.", e);
        }
    }

}
