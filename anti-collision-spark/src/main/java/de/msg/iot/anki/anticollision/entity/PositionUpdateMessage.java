package de.msg.iot.anki.anticollision.entity;


import java.util.List;

public class PositionUpdateMessage extends VehicleMessage {

    private int location;
    private int piece;
    private int speed;
    private int flags;
    private int lastLaneChangeCmd;
    private int lastExecLaneChangeCmd;
    private int lastDesiredHorizontalSpeed;
    private int lastDesiredSpeed;
    private float offset;
    private List<Distance> distances;

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getPiece() {
        return piece;
    }

    public void setPiece(int piece) {
        this.piece = piece;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getLastLaneChangeCmd() {
        return lastLaneChangeCmd;
    }

    public void setLastLaneChangeCmd(int lastLaneChangeCmd) {
        this.lastLaneChangeCmd = lastLaneChangeCmd;
    }

    public int getLastExecLaneChangeCmd() {
        return lastExecLaneChangeCmd;
    }

    public void setLastExecLaneChangeCmd(int lastExecLaneChangeCmd) {
        this.lastExecLaneChangeCmd = lastExecLaneChangeCmd;
    }

    public int getLastDesiredHorizontalSpeed() {
        return lastDesiredHorizontalSpeed;
    }

    public void setLastDesiredHorizontalSpeed(int lastDesiredHorizontalSpeed) {
        this.lastDesiredHorizontalSpeed = lastDesiredHorizontalSpeed;
    }

    public int getLastDesiredSpeed() {
        return lastDesiredSpeed;
    }

    public void setLastDesiredSpeed(int lastDesiredSpeed) {
        this.lastDesiredSpeed = lastDesiredSpeed;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public List<Distance> getDistances() {
        return distances;
    }

    public void setDistances(List<Distance> distances) {
        this.distances = distances;
    }
}
