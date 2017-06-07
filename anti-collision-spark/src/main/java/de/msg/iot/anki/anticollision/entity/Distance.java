package de.msg.iot.anki.anticollision.entity;

import java.io.Serializable;

public class Distance implements Serializable {

    private float horizontal;
    private float vertical;
    private float delta;

    public float getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(float horizontal) {
        this.horizontal = horizontal;
    }

    public float getVertical() {
        return vertical;
    }

    public void setVertical(float vertical) {
        this.vertical = vertical;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }
}
