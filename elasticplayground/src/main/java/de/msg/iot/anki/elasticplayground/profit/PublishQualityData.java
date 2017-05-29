package de.msg.iot.anki.elasticplayground.profit;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.msg.iot.anki.elasticplayground.entity.QualityEntry;
import org.bson.Document;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.concurrent.Callable;
import java.util.function.Consumer;


public class PublishQualityData implements Callable<Boolean> {

    private final MongoClient mongo;
    private final EntityManager mysql;

    public PublishQualityData() {
        this.mysql = Persistence.createEntityManagerFactory("anki")
                .createEntityManager();

        mongo = new MongoClient("localhost", 27017);
    }

    @Override
    public Boolean call() {

        try {
            mysql.getTransaction().begin();
            mysql.createNativeQuery("truncate table QUALITYENTRY ")
                    .executeUpdate();
            mysql.getTransaction().commit();

            MongoDatabase db = mongo.getDatabase("anki");
            MongoCollection<Document> collection = db.getCollection("message-quality");

            collection.find().forEach((Consumer<? super Document>) document -> {
                try {
                    mysql.getTransaction().begin();
                    QualityEntry entry = new QualityEntry();
                    entry.setLabel(Integer.parseInt(String.valueOf(document.get("label"))));
                    entry.setPosition(document.getString("position"));
                    entry.setSpeed(Integer.parseInt(String.valueOf(document.get("speed"))));
                    entry.setQuality(Double.parseDouble((String.valueOf(document.get("quality")))));
                    mysql.persist(entry);
                    mysql.getTransaction().commit();
                } catch (Exception e) {
                    System.err.println("Cannot store QualityEntry.");
                }
            });
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        } finally {
            mysql.close();
            mongo.close();
            return true;
        }

    }
}
