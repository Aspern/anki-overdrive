package de.msg.iot.anki.anticollision.entity;


import java.util.Date;

public class VehicleCommand {

    private String name;
    private Date timestamp;
    private int[] params;

    public VehicleCommand(String name) {
        this.name = name;
        this.timestamp = new Date();
    }

    public VehicleCommand(String name, Date timestamp, int... params) {
        this(name);
        this.params = params;
        this.timestamp = timestamp;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getParams() {
        return params;
    }

    public void setParams(int[] params) {
        this.params = params;
    }
}
