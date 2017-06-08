package de.msg.iot.anki.ml;


import com.google.inject.AbstractModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.msg.iot.anki.settings.Settings;
import de.msg.iot.anki.settings.properties.PropertiesSettings;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class MachineLearningModule extends AbstractModule {

    private final MongoDatabase db;
    private final Settings settings;
    private final Client elastic;

    public MachineLearningModule() {
        settings = new PropertiesSettings("settings.properties");
        MongoClient mongo = new MongoClient(
                settings.get("mongo.host", "localhost"),
                settings.getAsInt("mongo.port", 27017)
        );
        db = mongo.getDatabase(
                settings.get("mongo.db", "anki")
        );

        try {

            elastic = new PreBuiltTransportClient(org.elasticsearch.common.settings.Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName(
                                    settings.get("elastic.host", "localhost")
                            ),
                            settings.getAsInt("elastic.port", 9300)
                    ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void configure() {
        bind(Settings.class).toInstance(settings);
        bind(MongoDatabase.class).toInstance(db);
        bind(Client.class).toInstance(elastic);
    }
}
