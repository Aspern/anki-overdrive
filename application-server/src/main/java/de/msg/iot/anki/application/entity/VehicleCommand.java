package de.msg.iot.anki.application.entity;


public class VehicleCommand {

    private String name;
    private Number[] params;

    public VehicleCommand(String name) {
        this.name = name;
    }

    public VehicleCommand(String name, Number... params) {
        this(name);
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Number[] getParams() {
        return params;
    }

    public void setParams(Number[] params) {
        this.params = params;
    }
}
