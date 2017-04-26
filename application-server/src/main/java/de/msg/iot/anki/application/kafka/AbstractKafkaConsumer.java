package de.msg.iot.anki.application.kafka;


import com.google.gson.Gson;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.properties.PropertiesSettings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public abstract class AbstractKafkaConsumer<T> implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final String[] topics;
    private final Gson serializer = new Gson();
    private volatile boolean running = true;

    public AbstractKafkaConsumer(String... topics) {
        final Settings settings = new PropertiesSettings("settings.properties");
        this.topics = topics;

        Properties props = new Properties();
        props.put("bootstrap.servers", settings.get("kafka.server"));
        props.put("group.id", settings.get("kafka.group.id"));
        props.put("enable.auto.commit", settings.get("kafka.autocommit"));
        props.put("auto.commit.interval.ms", settings.get("kafka.commit.interval"));
        props.put("session.timeout.ms", settings.get("kafka.session.timeout"));
        props.put("key.deserializer", settings.get("kafka.key.deserializer"));
        props.put("value.deserializer", settings.get("kafka.value.deserializer"));

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Arrays.asList(topics));
    }


    @Override
    public void run() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    T value = serializer.fromJson(record.value(), getType());
                    handle(value);
                }
            }
        } finally {
            consumer.close();
        }
    }

    public abstract Class<T> getType();

    public abstract void handle(T record);

    public void stop() {
        this.running = false;
    }

}
