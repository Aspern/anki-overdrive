package de.msg.iot.anki.application.kafka;


import de.msg.iot.anki.application.entity.VehicleCommand;

public class VehicleCommandKafkaProducer extends AbstractKafkaProducer<VehicleCommand> {

    public VehicleCommandKafkaProducer(String vehicleId) {
        super(vehicleId);
    }


}
