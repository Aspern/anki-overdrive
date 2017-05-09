package de.msg.iot.anki.application.entity;


public class VehicleCommand {

    private String name;
    private int[] params;

    public VehicleCommand(String name) {
        this.name = name;
    }

    public VehicleCommand(String name, int... params) {
        this(name);
        this.params = params;
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
