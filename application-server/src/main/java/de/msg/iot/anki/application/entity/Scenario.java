package de.msg.iot.anki.application.entity;

public class Scenario {

    private String name;
    private boolean interrupt;

    public Scenario(String name, boolean interrupt) {
        this.name = name;
        this.interrupt = interrupt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }
}
