package de.msg.iot.anki.anticollision.kafka;


import com.google.gson.Gson;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.properties.PropertiesSettings;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.UUID;

public abstract class AbstractKafkaProducer<T> implements AutoCloseable {

    private  final String topic;
    private final Gson serializer = new Gson();
    private final KafkaProducer<String, String> producer;

    protected AbstractKafkaProducer(String topic) {
        this.topic = topic;

        final Settings settings = new PropertiesSettings("settings.properties");

        Properties props = new Properties();
        props.put("bootstrap.servers", settings.get("kafka.server"));
        props.put("client.id", UUID.randomUUID().toString());
        props.put("key.serializer", settings.get("kafka.key.serializer"));
        props.put("value.serializer", settings.get("kafka.value.serializer"));

        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(T data) {
        this.producer.send(new ProducerRecord<>(
                this.topic,
                0,
                UUID.randomUUID().toString(),
                serializer.toJson(data)
        ));
    }

    @Override
    public void close() throws Exception {
        if (this.producer != null)
            this.producer.close();
    }
}
