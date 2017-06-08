package de.msg.iot.anki.ml.batchview;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.client.MongoDatabase;
import de.msg.iot.anki.ml.MachineLearningModule;
import de.msg.iot.anki.ml.track.Finish;
import de.msg.iot.anki.ml.track.Piece;
import de.msg.iot.anki.ml.track.Start;
import de.msg.iot.anki.ml.track.Track;
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

public class MissingPointsBatchView extends IncrementalMongoBatchView {

    private final Client elastic;
    private final Logger logger = Logger.getLogger(MissingPointsBatchView.class);
    private final Settings settings;
    private String timestamp;
    private int lane = -1;
    private AtomicInteger k = new AtomicInteger(0);
    private List<Document> cache = new ArrayList<>();
    private final Track track = Track.builder()
            .addStraight(1)
            .addCurve(1)
            .addCurve(2)
            .addStraight(2)
            .addStraight(2)
            .addCurve(2)
            .addCurve(2)
            .build();
    private Piece current = track.getStart();

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
                .must(QueryBuilders.termQuery("messageId", 39))
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
        hits = filterFirstRecords(hits);

        do {
            increment.addAll(addMissingPoints(hits));

            scroll = elastic.prepareSearchScroll(scroll.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();
            hits = Arrays.asList(scroll.getHits().getHits());

            if (scroll.getHits().getHits().length < 100)
                hits = filterLastRecords(hits);

        } while (scroll.getHits().getHits().length != 0);

        return increment;
    }

    private List<Document> addMissingPoints(List<SearchHit> hits) {
        List<Document> increment = new ArrayList<>();

        if (hits.isEmpty())
            return increment;

        if (lane < 0)
            lane = (int) hits.get(0).getSource().get("lane");

        for (int i = 0; i < hits.size(); i++) {
            SearchHit hit = hits.get(i);

            do {
                final Map<String, Object> source = hit.getSource();
                final int piece = current.getId();
                final int location = current.getLanes()[lane][k.get()];
                k.incrementAndGet();

                if ((int) source.get("piece") == piece
                        && (int) source.get("location") == location) {
                    cache.add(createEntry(source));
                    if (i + 1 < hits.size()) {
                        i++;
                        hit = hits.get(i);
                    }
                } else {
                    cache.add(createMissingEntry(source, piece, location));
                }

                if (current.getLanes()[lane].length <= k.get()) {
                    k.set(0);
                    current = current.getNext();
                }

            } while (current != track.getStart());
        }

        return increment;
    }

    private Document createEntry(Map<String, Object> hit) {
        Document document = new Document();
        document.put("piece", hit.get("piece"));
        document.put("location", hit.get("location"));
        document.put("lastDesiredSpeed", hit.get("lastDesiredSpeed"));
        document.put("timestamp", hit.get("timestamp"));
        document.put("missing", false);
        return document;
    }

    private Document createMissingEntry(Map<String, Object> hit, int piece, int location) {
        Document document = new Document();
        document.put("piece", piece);
        document.put("location", location);
        document.put("lastDesiredSpeed", hit.get("lastDesiredSpeed"));
        document.put("timestamp", hit.get("timestamp"));
        document.put("missing", true);
        return document;
    }

    private List<SearchHit> filterFirstRecords(List<SearchHit> hits) {
        int index = 0;
        for (; index < hits.size(); index++)
            if ((int) hits.get(index).getSource().get("piece") == Start.START_ID)
                break;

        return hits.subList(index, hits.size() - 1);
    }

    private List<SearchHit> filterLastRecords(List<SearchHit> hits) {
        int index = hits.size() - 1;
        for (; index >= 0; index--)
            if ((int) hits.get(index).getSource().get("piece") == Finish.FINISH_ID)
                break;

        if (index < 0)
            index = 0;

        return hits.subList(0, index);
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
