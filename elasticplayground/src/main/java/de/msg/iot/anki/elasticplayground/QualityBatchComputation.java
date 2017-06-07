package de.msg.iot.anki.elasticplayground;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

public class QualityBatchComputation {

    private final static Logger logger = Logger.getLogger(QualityBatchComputation.class);

    public static class Position {
        private final int piece;
        private final int location;

        public Position(int piece, int location) {
            this.piece = piece;
            this.location = location;
        }

        public int piece() {
            return piece;
        }

        public int location() {
            return location;
        }

        @Override
        public String toString() {
            return piece + ":" + location;
        }
    }

    private static class Computation {
        private final int speed;
        private final String position;

        public Computation(String position, int speed) {
            this.speed = speed;
            this.position = position;
        }

        public int getSpeed() {
            return speed;
        }

        public String getPosition() {
            return position;
        }
    }

    public static Client elastic;
    private static MongoClient mongo = new MongoClient("localhost", 27017);

    static {
        try {
            elastic = new PreBuiltTransportClient(org.elasticsearch.common.settings.Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName("localhost"),
                            9300
                    ));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }


    public static void main(String[] args) throws Exception {

        try {
            logger.info("Starting computation.");
            final List<Position> positions = new ArrayList<>();

            final SearchResponse response = elastic.prepareSearch("anki")
                    .setTypes("roundUpdate")
                    .setSize(0)
                    .addAggregation(
                            AggregationBuilders.terms("pieces")
                                    .field("labeledPositions.piece")
                                    .size(50)
                                    .subAggregation(
                                            AggregationBuilders.terms("locations")
                                                    .field("labeledPositions.location")
                                                    .size(50)
                                    )
                    ).get();

            Terms pieces = response.getAggregations().get("pieces");

            pieces.getBuckets().forEach(bucket -> {
                Terms locations = bucket.getAggregations().get("locations");
                locations.getBuckets().forEach(subBucket -> {
                    int piece = Integer.parseInt(bucket.getKeyAsString());
                    int location = Integer.parseInt(subBucket.getKeyAsString());
                    positions.add(new Position(piece, location));
                });
            });

            logger.info("Found positions: " + positions);
            final MongoDatabase db = mongo.getDatabase("anki");
            final MongoCollection<Document> collection = db.getCollection("position-speed");
            collection.drop();

            collection.insertMany(positions.parallelStream()
                    .map(position -> new Computation(position.toString(), getMaxSpeedForPosition(position)))
                    .map(computation -> {
                        Document document = new Document();
                        document.put("position", computation.getPosition());
                        document.put("speed", computation.getSpeed());
                        return document;
                    }).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error(e);
        } finally {
            elastic.close();
            mongo.close();
        }
    }

    public static int getMaxSpeedForPosition(Position position) {


        final List<Integer> tmp = new ArrayList<>();

        SearchResponse scroll = elastic.prepareSearch("anki")
                .setTypes("roundUpdate")
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(1000)
                .get();

        do {
            tmp.add(Arrays.asList(
                    scroll.getHits().getHits())
                    .stream()
                    .map(hit -> hit.getSource())
                    .map(source -> (List<Map<String, Object>>) source.get("labeledPositions"))
                    .flatMap(labeledPositions -> labeledPositions.stream())
                    .filter(labeledPosition -> (int) labeledPosition.get("piece") == position.piece())
                    .filter(labeledPosition -> (int) labeledPosition.get("location") == position.location())
                    .filter(labeledPosition -> !(boolean) labeledPosition.get("missing"))
                    .map(labeledPosition -> (int) labeledPosition.get("speed"))
                    .max(Comparator.comparingInt(o -> o))
                    .orElse(400)
            );

            scroll = elastic.prepareSearchScroll(scroll.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .get();

        } while (scroll.getHits().getHits().length != 0);

        return tmp.stream()
                .max(Comparator.comparingInt(o -> o))
                .get();
    }


}
