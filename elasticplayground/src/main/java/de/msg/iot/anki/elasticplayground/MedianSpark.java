package de.msg.iot.anki.elasticplayground;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class MedianSpark {

    public static void main(String[] args) throws Exception {

        List<Long> latencies = Files.readAllLines(
                Paths.get(
                        MedianSpark.class.getClassLoader().getResource("latencies-spark.txt").toURI()
                )
        ).stream()
                .map(s -> Long.parseLong(s))
                .sorted()
                .collect(Collectors.toList());

        System.out.println("Median: " + latencies.get(latencies.size() / 2));

        double avg = latencies.stream()
                .reduce((l1, l2) -> l1 + l2)
                .map(l -> (double)l)
                .map(l -> l / latencies.size())
                .get();

        System.out.println("Average: " + avg);


    }
}
