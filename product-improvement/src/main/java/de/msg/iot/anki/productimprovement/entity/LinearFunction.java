package de.msg.iot.anki.productimprovement.entity;

public class LinearFunction {


    private final double intercept;
    private final double slope;
    private final double correlation;
    private final String position;

    public LinearFunction(double intercept, double slope, double correlation, String position) {
        this.intercept = intercept;
        this.slope = slope;
        this.position = position;
        this.correlation = correlation;
    }

    public double getIntercept() {
        return intercept;
    }

    public String getPosition() {
        return position;
    }

    public double getSlope() {
        return slope;
    }

    public double getCorrelation() {
        return correlation;
    }
}
