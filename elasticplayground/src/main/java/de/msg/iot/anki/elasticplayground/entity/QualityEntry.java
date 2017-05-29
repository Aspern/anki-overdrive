package de.msg.iot.anki.elasticplayground.entity;

import javax.persistence.*;

@Entity
public class QualityEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    private String position;
    private double quality;
    private int speed;
    private int label;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
