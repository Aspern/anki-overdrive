package de.msg.iot.anki.productimprovement.ml;


import de.msg.iot.anki.productimprovement.entity.AccelerationCommand;
import de.msg.iot.anki.productimprovement.entity.LinearFunction;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import java.util.concurrent.Callable;

public class MLWorker implements Callable<LinearFunction> {

    private final InstanceQuery query;
    private final String position;

    public MLWorker(String position) {
        this.position = position;

        try {
            this.query = new InstanceQuery();
            this.query.setUsername("aweber");
            this.query.setPassword("anki");
            this.query.setQuery("SELECT a1_1,vi_1 FROM "
                    + AccelerationCommand.class.getSimpleName().toUpperCase()
                    + " WHERE p1 = " + position);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LinearFunction call() throws Exception {

        Instances instances = this.query.retrieveInstances();
        instances.setClassIndex(0);

        SimpleLinearRegression model = new SimpleLinearRegression();
        model.setOutputAdditionalStats(true);
        model.buildClassifier(instances);

        Evaluation evaluation = new Evaluation(instances);
        evaluation.evaluateModel(model, instances);

        return new LinearFunction(
                model.getIntercept(),
                model.getSlope(),
                evaluation.correlationCoefficient(),
                position
        );
    }

}
