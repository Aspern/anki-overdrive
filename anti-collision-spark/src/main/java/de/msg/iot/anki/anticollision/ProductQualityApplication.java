package de.msg.iot.anki.anticollision;


import com.google.gson.Gson;
import de.msg.iot.anki.anticollision.entity.RoundUpdate;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.properties.PropertiesSettings;
import org.apache.commons.collections.map.HashedMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.StreamingLinearRegressionWithSGD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class ProductQualityApplication {

    private final static Gson serializer = new Gson();

    public static void main(String[] args) throws Exception {


        final SparkConf sparkConf = new SparkConf()
                .setAppName(ProductQualityApplication.class.getSimpleName())
                .setMaster("local");

        final JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(50));
        final Settings settings = new PropertiesSettings("settings.properties");
        final Map<String, Object> kafkaParams = new HashedMap();

        jssc.sparkContext().setLogLevel("OFF");

        kafkaParams.put("bootstrap.servers", settings.get("kafka.server"));
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", UUID.randomUUID().toString());
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", settings.getAsBoolean("kafka.autocommit", false));

        final JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(
                                Arrays.asList("vehicle-data"),
                                kafkaParams
                        )
                );

        final Thread thread = new Thread(() -> {
            try {
                JavaDStream<LabeledPoint> trainingData = stream.map(record -> record.value())
                        .filter(message -> message.contains("roundUpdate"))
                        .map(message -> serializer.fromJson(message, RoundUpdate.class))
                        .flatMap(roundUpdate -> roundUpdate.getLabeledPositions().iterator())
                        .map(labeledPositionUpdate -> new LabeledPoint(
                                labeledPositionUpdate.isMissing() ? 0 : 1,
                                Vectors.dense(
                                        labeledPositionUpdate.getSpeed()
                                )
                        ));

                final StreamingLinearRegressionWithSGD model = new StreamingLinearRegressionWithSGD();
                model.setInitialWeights(Vectors.zeros(2));

                model.trainOn(trainingData);

                jssc.start();
                jssc.awaitTermination();
                model.model().get().save(jssc.sparkContext().sc(), "./test-model");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });

        final Scanner scanner = new Scanner(System.in);
        scanner.next();
        jssc.stop();

    }


}
