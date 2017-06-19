package de.msg.iot.anki.spark.kafka;

import de.msg.iot.anki.settings.Settings;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by msg on 17.03.17.
 */
public class KafkaProducer {

    private String topic;
    private Producer producer;
    private Properties props;

    public KafkaProducer(Settings settings){
        this.topic = settings.get("kafka.topic");

        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.get("kafka.server"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, settings.get("kafka.key.serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, settings.get("kafka.value.serializer"));
        props.put(ProducerConfig.ACKS_CONFIG, settings.get("kafka.ack_config"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, settings.get("kafka.client_id_config"));  // For figuring out exception

        producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(props);
    }

    public KafkaProducer(Settings settings, String topic){
        this.topic = topic;

        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.get("kafka.server"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, settings.get("kafka.key.serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, settings.get("kafka.value.serializer"));
        props.put(ProducerConfig.ACKS_CONFIG, settings.get("kafka.ack_config"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, settings.get("kafka.client_id_config"));  // For figuring out exception

        producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(props);
    }

    public void sendMessage(String message){
        ProducerRecord<String, String> rec = new ProducerRecord<>(topic, "key", message);
        producer.send(rec);
    }

    public void sendMessage(String message, String topic){
        ProducerRecord<String, String> rec = new ProducerRecord<>(topic, "key", message);
        producer.send(rec);
    }

    /*
    * Don't forget to close this!
    * */
    public void close(){
        producer.close();
    }
}
