package de.msg.iot.anki.elasticplayground.entity;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OptimalSpeed {

    @Id
    private String position;
    private int speed;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
