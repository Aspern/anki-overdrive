package de.msg.iot.la.batchview.mongo;

import com.mongodb.client.MongoCollection;
import de.msg.iot.la.batchview.IncrementalBatchView;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public abstract class IncrementalMongoBatchView extends IncrementalBatchView<Document> {

    private final MongoCollection<Document> collection;

    protected IncrementalMongoBatchView(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public Collection<Document> fetch() {
        final Collection<Document> list = new ArrayList<>();
        collection.find().forEach((Consumer<? super Document>) document -> list.add(document));

        return list;
    }

    @Override
    public void forEach(Consumer<Document> consumer) {
        collection.find().forEach((Consumer<? super Document>) document -> consumer.accept(document));
    }

    @Override
    public void drop() {
        collection.drop();
    }

    @Override
    protected void addIncrement(Collection<Document> increment) {
        if(increment.isEmpty())
            return;

        collection.insertMany(new ArrayList<>(increment));
    }
}
