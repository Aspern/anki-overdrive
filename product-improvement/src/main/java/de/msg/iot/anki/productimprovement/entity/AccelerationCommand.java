package de.msg.iot.anki.productimprovement.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class AccelerationCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Date timestamp;
    private String p1;
    private String p2;
    private int ai_1;
    private int vi;
    private int vi_1;
    private int v0;
    private double score;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public int getAi_1() {
        return ai_1;
    }

    public void setAi_1(int ai_1) {
        this.ai_1 = ai_1;
    }

    public int getVi() {
        return vi;
    }

    public void setVi(int vi) {
        this.vi = vi;
    }

    public int getVi_1() {
        return vi_1;
    }

    public void setVi_1(int vi_1) {
        this.vi_1 = vi_1;
    }

    public int getV0() {
        return v0;
    }

    public void setV0(int v0) {
        this.v0 = v0;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
