package de.msg.iot.anki.anticollision.entity;


public class LabeledPositionUpdate extends PositionUpdateMessage {

    private boolean missing;

    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }
}
