package de.msg.iot.anki.anticollision;

import com.google.gson.Gson;
import de.msg.iot.anki.anticollision.common.LifeCycleComponent;
import de.msg.iot.anki.anticollision.entity.Distance;
import de.msg.iot.anki.anticollision.entity.PositionUpdateMessage;
import de.msg.iot.anki.anticollision.entity.VehicleCommand;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.properties.PropertiesSettings;
import org.apache.commons.collections.map.HashedMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AntiCollisionApplication implements LifeCycleComponent {


    private final static Map<String, Integer> lastDesiredSpeeds = new HashMap<>();
    private final static Logger logger = Logger.getLogger(AntiCollisionApplication.class);
    private final static KafkaProducer<String, String> producer;

    static {
        final Settings settings = new PropertiesSettings("settings.properties");

        Properties props = new Properties();
        props.put("bootstrap.servers", settings.get("kafka.server"));
        props.put("client.id", UUID.randomUUID().toString());
        props.put("key.serializer", settings.get("kafka.key.serializer"));
        props.put("value.serializer", settings.get("kafka.value.serializer"));
        producer = new KafkaProducer<>(props);
    }

    private final static Gson serializer = new Gson();


    private final JavaStreamingContext jssc;
    private final Map<String, Object> kafkaParams;
    private final ExecutorService threadpool = Executors.newSingleThreadExecutor();

    private final String vehicleTopic;

    private volatile boolean running = false;


    public AntiCollisionApplication() {

        final Settings settings = new PropertiesSettings("settings.properties");
        final SparkConf sparkConf = new SparkConf()
                .setAppName(name())
                .setMaster(settings.get("spark.master", "local"));

        jssc = new JavaStreamingContext(sparkConf, new Duration(
                settings.getAsInt("spark.batch.duration", 25)
        ));
        jssc.sparkContext().setLogLevel("OFF");

        vehicleTopic = settings.get("kafka.vehicle.topic", "vehicle-data");

        kafkaParams = new HashedMap();
        kafkaParams.put("bootstrap.servers", settings.get("kafka.server"));
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", UUID.randomUUID().toString());
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", settings.getAsBoolean("kafka.autocommit", false));
    }


    @Override
    public void start() {
        threadpool.submit(() -> {
            if (running) {
                logger.warn(name() + " is already running.");
                return;
            }

            logger.info("Starting " + name() + ".");

            try {
                final JavaInputDStream<ConsumerRecord<String, String>> stream =
                        KafkaUtils.createDirectStream(
                                jssc,
                                LocationStrategies.PreferConsistent(),
                                ConsumerStrategies.<String, String>Subscribe(
                                        Arrays.asList(vehicleTopic),
                                        kafkaParams
                                )
                        );


                stream.map(
                        (Function<ConsumerRecord<String, String>, String>) record -> record.value()
                ).filter(
                        message -> message.contains("positionUpdate")
                ).map(
                        message -> serializer.fromJson(message, PositionUpdateMessage.class)
                ).foreachRDD((positionUpdateMessageJavaRDD, time) -> positionUpdateMessageJavaRDD.foreach(message -> {
                    if (!lastDesiredSpeeds.containsKey(message.getVehicleId())) {
                        System.out.println("Adding lastDesiredSpeed [" + message.getLastDesiredSpeed()
                                + "] for vehicle [" + message.getVehicleId() + "].");
                        lastDesiredSpeeds.put(message.getVehicleId(), message.getLastDesiredSpeed());
                    }

                    if (message.getDistances() == null || message.getDistances().isEmpty()) {
                        System.err.println("Distances are empty!!!");
                        return;
                    }


                    for (Distance distance : message.getDistances()) {
                        if (distance.getVertical() < 34
                                && distance.getDelta() < 0
                                && distance.getHorizontal() < 500) {
                            producer.send(new ProducerRecord<>(
                                    message.getVehicleId(),
                                    0,
                                    UUID.randomUUID().toString(),
                                    "{\"name\":\"brake\",\"params\":[50,300],\"timestamp\":" + message.getTimestamp().getTime() + "}"
                                    // serializer.toJson(new VehicleCommand("brake", message.getTimestamp(), 50, 300))
                            ));

                        } else if (distance.getHorizontal() > 700) {
                            producer.send(new ProducerRecord<>(
                                    message.getVehicleId(),
                                    0,
                                    UUID.randomUUID().toString(),
                                    "{\"name\":\")\",\"params\":["+ lastDesiredSpeeds.get(message.getVehicleId()) + ",50],\"timestamp\":" + message.getTimestamp().getTime() + "}"
//                                    serializer.toJson(
//                                            new VehicleCommand("accelerate", message.getTimestamp(), lastDesiredSpeeds.get(message.getVehicleId()), 50)
//                                    )
                            ));
                        }
                    }
                }));

                jssc.start();
                running = true;
                jssc.awaitTermination();
            } catch (Exception e) {
                logger.error("Error while executing " + name() + ".", e);
                e.printStackTrace(System.err);
                stop();
            }
        });
    }

    @Override
    public void stop() {
        if (!running)
            return;

        try {

            if (jssc != null)
                jssc.stop();

            threadpool.shutdown();
            threadpool.awaitTermination(1, TimeUnit.MINUTES);


            running = false;
        } catch (Exception e) {
            System.err.println("Error while stopping " + name() + ".");
            e.printStackTrace(System.err);
        }

    }

    public String name() {
        return getClass().getSimpleName();
    }

    public static void main(String[] args) {
        AntiCollisionApplication application = new AntiCollisionApplication();
        application.start();

        final Scanner scanner = new Scanner(System.in);
        scanner.next();
        application.stop();
    }

}
