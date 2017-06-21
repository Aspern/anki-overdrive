package de.msg.iot.anki.ml.batchview;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.client.MongoDatabase;
import de.msg.iot.anki.ml.MachineLearningModule;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.la.batchview.mongo.IncrementalMongoBatchView;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MissingPointsBatchView extends IncrementalMongoBatchView {

    private final Client elastic;
    private final Logger logger = Logger.getLogger(MissingPointsBatchView.class);
    private final Settings settings;
    private String timestamp;
    private int lane = -1;
    private AtomicInteger k = new AtomicInteger(0);
    private List<Document> cache = new ArrayList<>();


    @Inject
    public MissingPointsBatchView(Settings settings, Client client, MongoDatabase db) {
        super(db.getCollection(
                MissingPointsBatchView.class.getSimpleName()
        ));
        this.elastic = client;
        this.settings = settings;
        this.timestamp = settings.get("batchview.timestamp");
    }

    @Override
    protected Collection<Document> compute() throws Exception {
        Collection<Document> increment = new ArrayList<>();

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("messageId", -119))
                .must(QueryBuilders.rangeQuery("timestamp")
                        .gte(timestamp)
                        .lte("now")
                );

        SearchResponse scroll = elastic.prepareSearch(settings.get("elastic.index"))
                .addSort("timestamp", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(query)
                .setSize(100)
                .get();

        List<SearchHit> hits = Arrays.asList(scroll.getHits().getHits());

        do {
            increment.addAll(extractPositionData(hits));

            scroll = elastic.prepareSearchScroll(scroll.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();


        } while (scroll.getHits().getHits().length != 0);

        return increment;
    }

    private List<Document> extractPositionData(List<SearchHit> hits) {
//        return hits.stream()
//                .map(hit -> hit.getSource())
//                .flatMap(source -> ((List<Map<String, Object>>)source.get("labeledPositions")))
//                .map(labeledPosition -> {
//                    Document document = new Document();
//                    document.put("lastDesiredSpeed", labeledPosition.);
//                }).collect(Collectors.toList());
        return null;
    }


    @Override
    protected void handleException(Exception e) {
        logger.error("Exception while computation", e);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Injector injector = Guice.createInjector(new MachineLearningModule());
        MissingPointsBatchView batchView = injector.getInstance(MissingPointsBatchView.class);

        pool.submit(batchView);

        Scanner scanner = new Scanner(System.in);
        scanner.next();

        batchView.stop();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }
}
