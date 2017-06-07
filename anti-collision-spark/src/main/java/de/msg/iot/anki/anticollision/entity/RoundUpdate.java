package de.msg.iot.anki.anticollision.entity;

import java.util.List;

public class RoundUpdate extends VehicleMessage {

    private List<LabeledPositionUpdate> labeledPositions;
    private int round;
    private double quality;
    private int profit;

    public List<LabeledPositionUpdate> getLabeledPositions() {
        return labeledPositions;
    }

    public void setLabeledPositions(List<LabeledPositionUpdate> labeledPositions) {
        this.labeledPositions = labeledPositions;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }
}
