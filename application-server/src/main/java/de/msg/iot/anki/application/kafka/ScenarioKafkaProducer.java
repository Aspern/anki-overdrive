package de.msg.iot.anki.application.kafka;


import de.msg.iot.anki.application.entity.Scenario;

public class ScenarioKafkaProducer extends AbstractKafkaProducer<Scenario> {

    public ScenarioKafkaProducer() {
        super("scenario");
    }

    public void startAntiCollision() {
        this.sendMessage(new Scenario(
                "anti-collision",
                false
        ));
    }

    public void stopAntiCollision() {
        this.sendMessage(new Scenario(
                "anti-collision",
                true
        ));
    }

    public void startCollision() {
        this.sendMessage(new Scenario(
                "collision",
                false
        ));
    }

    public void stopCollision() {
        this.sendMessage(new Scenario(
                "collision",
                true
        ));
    }
}
