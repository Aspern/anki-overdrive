package de.msg.iot.anki.elasticplayground.profit;


import de.msg.iot.anki.elasticplayground.entity.OptimalSpeed;
import weka.classifiers.functions.LinearRegression;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import java.util.concurrent.Callable;

public class MachineLearningWorker implements Callable<OptimalSpeed> {

    private final String position;
    private final double expectedQuality;


    public MachineLearningWorker(String position, double expectedQuality) {
        this.position = position;
        this.expectedQuality = expectedQuality;
    }

    @Override
    public OptimalSpeed call() throws Exception {


        System.out.println("Starting " + getClass().getSimpleName() + " with position = " + position + "...");

        Instances instances = this.queryInstances();
        instances.setClassIndex(0);

        LinearRegression model = new LinearRegression();
        model.setOutputAdditionalStats(true);
        model.buildClassifier(instances);

        OptimalSpeed optimalSpeed = new OptimalSpeed();
        optimalSpeed.setPosition(position);

        DenseInstance instance = new DenseInstance(1);
        instance.setValue(0, expectedQuality);


        double[] coefficients = model.coefficients();
        optimalSpeed.setSpeed((int) (coefficients[1] * expectedQuality + coefficients[2]));
        return optimalSpeed;


    }

    private Instances queryInstances() throws Exception {
        InstanceQuery query = new InstanceQuery();
        query.setUsername("aweber");
        query.setPassword("anki");
        query.setQuery("SELECT speed, quality FROM QUALITYENTRY WHERE position = '" + position + "'");
        return query.retrieveInstances();
    }
}
