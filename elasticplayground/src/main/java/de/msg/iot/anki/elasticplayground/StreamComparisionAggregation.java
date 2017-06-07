package de.msg.iot.anki.elasticplayground;


import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class StreamComparisionAggregation {

    private String from;
    private String to;
    private final Client elastic;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public StreamComparisionAggregation() throws UnknownHostException {

        this.from = "2017-01-01T00:00:00.000Z";
        this.to = "now";

        this.elastic = new PreBuiltTransportClient(org.elasticsearch.common.settings.Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName("localhost"),
                        9300
                ));
    }

    public void compute() {

        final List<Long> latencies = new ArrayList<>();


        SearchResponse response = this.elastic.prepareSearch("anki")
                .setQuery(
                        QueryBuilders.boolQuery()
                                .must(QueryBuilders.matchQuery("messageName", "positionUpdate OR setSpeed"))
                                .must(QueryBuilders.termQuery("vehicleId", "ed0c94216553"))
                                .must(QueryBuilders.rangeQuery("timestamp")
                                        .gte(from)
                                        .lte(to)
                                )

                ).addAggregation(
                        AggregationBuilders
                                .stats("distance-stats")
                                .field("distances.horizontal")
                )
                .setSize(1000)
                .addSort("timestamp", SortOrder.ASC)
                .get();

        Map<String, Object> setSpeed = null;
        Map<String, Object> positionUpdate = null;
        List<Double> allDistances = new ArrayList<>();
        List<Long> timestampSetSpeed = new ArrayList<>();

        for (SearchHit hit : response.getHits().getHits()) {

            Map<String, Object> source = hit.getSource();

            if (source.containsKey("distances") && source.get("distances") != null) {
                List<Map<String, Object>> distances = (List<Map<String, Object>>) source.get("distances");
                if (!distances.isEmpty()) {
                    allDistances.add(
                            distances.get(0).get("horizontal") instanceof Integer ? (Integer) distances.get(0).get("horizontal") : (Double) distances.get(0).get("horizontal")
                    );
                }

            }

            if ("positionUpdate".equals(source.get("messageName"))) {
                positionUpdate = source;


                if (setSpeed != null && positionUpdate.get("lastDesiredSpeed").equals(setSpeed.get("speed"))) {
                    try {
                        latencies.add(
                                parseDate((String) positionUpdate.get("timestamp"))
                                        - parseDate((String) setSpeed.get("timestamp"))
                        );
                        setSpeed = null;
                    } catch (ParseException e) {
                        e.printStackTrace(System.err);
                    }
                }

            } else if ("setSpeed".equals(source.get("messageName"))) {
                setSpeed = source;
                try {
                    timestampSetSpeed.add(parseDate((String) setSpeed.get("timestamp")));
                } catch (ParseException e) {
                    e.printStackTrace(System.err);
                }

            }
        }
        List<Long> timestampStream = new ArrayList<>();
        try {
            timestampStream = Files.readAllLines(Paths.get(getClass().getResource("/latencies-spark.txt").toURI()))
                    .stream()
                    .map(s -> Long.parseLong(s))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }


        timestampSetSpeed = timestampSetSpeed.stream()
                .map(aLong -> aLong + (2 * 60 * 60 * 1000)).collect(Collectors.toList());


        timestampSetSpeed.sort((o1, o2) -> o1 > o2 ? 1 : 0);
        timestampStream.sort((o1, o2) -> o1 > o2 ? 1 : 0);


        int i = 0;
        for (; i < timestampStream.size(); i++)
            if (timestampSetSpeed.get(0) <= timestampStream.get(i))
                break;

        for (int j = 0; j < timestampSetSpeed.size(); j++, i++) {
            String line = "" + timestampSetSpeed.get(j);
            if (i < timestampStream.size()) {
                line += " - " + timestampStream.get(i);
                line += " = " + Math.abs(timestampSetSpeed.get(j) - timestampStream.get(i));
            }
            System.out.println(line);
        }


        long avgLatency = latencies.stream()
                .reduce((l1, l2) -> l1 + l2)
                .map(l -> l / latencies.size())
                .get();

        latencies.sort((o1, o2) -> (int) (o1 - o2));


        Stats stats = response.getAggregations().get("distance-stats");

        double minDistance = stats.getMin();
        double expectedDistance = 500;


        double mae = allDistances.stream()
                .map(v -> Math.abs(expectedDistance - v))
                .reduce((v1, v2) -> v1 + v2)
                .map(v -> v / allDistances.size())
                .get();

        System.out.println("avgLatency [ms] \t minDistance [mm] \t MAE [mm]");
        System.out.println("-----------------------------------------");
        System.out.println(avgLatency + "\t\t\t\t" + minDistance + "\t\t" + mae);


    }

    public StreamComparisionAggregation from(int year, int month, int day, int hour, int minute, int second, int millis) {
        return setDate("from", year, month, day, hour, minute, second, millis);
    }

    public StreamComparisionAggregation from(int year, int month, int day, int hour, int minute, int second) {
        return from(year, month, day, hour, minute, second, 0);
    }

    public StreamComparisionAggregation from(int year, int month, int day, int hour, int minute) {
        return from(year, month, day, hour, minute, 0);
    }

    public StreamComparisionAggregation to(int year, int month, int day, int hour, int minute, int second, int millis) {
        return setDate("to", year, month, day, hour, minute, second, millis);
    }

    public StreamComparisionAggregation to(int year, int month, int day, int hour, int minute, int second) {
        return to(year, month, day, hour, minute, second, 0);
    }

    public StreamComparisionAggregation to(int year, int month, int day, int hour, int minute) {
        return to(year, month, day, hour, minute, 0);
    }

    public StreamComparisionAggregation toNow() {
        this.to = "now";
        return this;
    }

    private long parseDate(String dateString) throws ParseException {
        return this.format.parse(dateString).getTime();
    }

    private StreamComparisionAggregation setDate(String key, int year, int month, int day, int hour, int minute, int second, int millis) {
        final StringBuilder builder = new StringBuilder();
        builder.append(formatNumber(year));
        builder.append("-");
        builder.append(formatNumber(month));
        builder.append("-");
        builder.append(formatNumber(day));
        builder.append("T");
        builder.append(formatNumber(hour));
        builder.append(":");
        builder.append(formatNumber(minute));
        builder.append(":");
        builder.append(formatNumber(second));
        builder.append(".");
        builder.append(formatNumber(millis, 3));
        builder.append("Z");
        if ("to".equals(key)) {
            this.to = builder.toString();
        } else if ("from".equals(key)) {
            this.from = builder.toString();
        }
        return this;
    }

    private String formatNumber(int number) {
        return formatNumber(number, 2);
    }

    private String formatNumber(int number, int width) {
        return String.format("%0" + width + "d", number);
    }


    public static void main(String[] args) throws Exception {

        new StreamComparisionAggregation()
                .from(2017, 5, 19, 7, 40, 40)
                .to(2017, 5, 19, 7, 45, 40)
                .compute();

    }
}
