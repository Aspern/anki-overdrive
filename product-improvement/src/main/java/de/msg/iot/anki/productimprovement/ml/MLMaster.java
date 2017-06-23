package de.msg.iot.anki.productimprovement.ml;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import de.msg.iot.anki.productimprovement.entity.AccelerationCommand;
import de.msg.iot.anki.productimprovement.entity.LinearFunction;
import de.msg.iot.anki.productimprovement.preprocess.ScorePreProcessor;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MLMaster implements Runnable {

    private final Logger logger = Logger.getLogger(MLMaster.class);
    private final Collection<Future<LinearFunction>> results;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public MLMaster() {
        this.results = new ArrayList<>();
    }

    @Override
    public void run() {
        try {

            logger.info("Starting " + getClass().getSimpleName() + ".");
            final ScorePreProcessor preProcessor = new ScorePreProcessor();
            preProcessor.start();
            preProcessor.awaitTerminationOr(error -> logger.error("Errors while pre-processing data.", error));

            logger.info("Searching positions for worker.");
            List<String> positions = findAllPositions();
            logger.info(positions.toString());
            positions.forEach(position -> {
                results.add(
                        threadPool.submit(new MLWorker(position))
                );
            });


            threadPool.shutdown();
            threadPool.awaitTermination(30, TimeUnit.MINUTES);

            logger.info("Storing models in Batch-View.");
            storeInBatchView(
                    results.stream()
                            .map(future -> {
                                try {
                                    return future.get();
                                } catch (Exception e) {
                                    logger.error("Errors in future.", e);
                                    return null;
                                }
                            }).collect(Collectors.toList())

            );

            logger.info("Finished " + getClass().getSimpleName() + ".");

        } catch (Exception e) {
            logger.error("Error while computing ML models.", e);
        }
    }

    public List<String> findAllPositions() {
        List<String> positions = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            logger.error("Cannot load driver com.mysql.jdbc.Driver.", e);
        }

        try (
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/anki", "aweber", "anki");
                PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT p1 FROM " + AccelerationCommand.class.getSimpleName().toUpperCase());
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                positions.add(result.getString(1));
            }
        } catch (Exception e) {
            logger.error("Errors while searching all positions.", e);
        }

        return positions;
    }

    private void storeInBatchView(Collection<LinearFunction> functions) {
        final MongoClient mongo = new MongoClient("localhost", 27017);

        MongoCollection<Document> collection = mongo.getDatabase("anki")
                .getCollection("acceleration-model");

        functions.stream()
                .filter(linearFunction -> linearFunction != null)
                .map(function -> new Document().append("position", function.getPosition())
                        .append("intercept", function.getIntercept())
                        .append("slope", function.getSlope())
                        .append("correlation", function.getCorrelation())

                ).forEach((Document document) -> {
            Document existing = collection.find(document).first();

            if (existing == null) {
                collection.insertOne(document);
            } else if ((double) existing.get("correlation") < (double) document.get("correlation")) {
                collection.updateOne(existing, document);
            }
        });
    }
}
