package de.msg.iot.anki.application.kafka;


import de.msg.iot.anki.application.entity.Setup;

import java.util.function.Consumer;

public class SetupKafkaConsumer extends AbstractKafkaConsumer<Setup> {

    private Consumer<Setup> consumer;

    public SetupKafkaConsumer() {
        super("setup");
    }

    @Override
    public Class<Setup> getType() {
        return Setup.class;
    }

    @Override
    public void handle(Setup record) {
        if (consumer != null)
            consumer.accept(record);
    }

    public void handle(Consumer<Setup> consumer) {
        this.consumer = consumer;
    }
}
