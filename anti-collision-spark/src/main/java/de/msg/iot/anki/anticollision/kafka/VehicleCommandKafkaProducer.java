package de.msg.iot.anki.anticollision.kafka;


import de.msg.iot.anki.anticollision.entity.VehicleCommand;

public class VehicleCommandKafkaProducer extends AbstractKafkaProducer<VehicleCommand> {

    public VehicleCommandKafkaProducer(String vehicleId) {
        super(vehicleId);
    }


}
