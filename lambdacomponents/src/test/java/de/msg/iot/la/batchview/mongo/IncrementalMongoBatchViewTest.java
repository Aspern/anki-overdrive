package de.msg.iot.la.batchview.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.msg.iot.la.batchview.IncrementalBatchView;
import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IncrementalMongoBatchViewTest {

    private static IncrementalBatchView batchView;

    static class TestModule extends AbstractModule {

        private final MongoDatabase db;

        public TestModule() {
            MongoClient mongo = new MongoClient("localhost", 27017);
            db = mongo.getDatabase("anki");
        }

        @Override
        protected void configure() {
            bind(MongoDatabase.class).toInstance(db);
        }

    }

    static class TestIncrementalMongoBatchView extends IncrementalMongoBatchView {

        @Inject
        public TestIncrementalMongoBatchView(MongoDatabase db) {
            super(
                    db.getCollection(
                            TestIncrementalMongoBatchView.class.getSimpleName()
                    )
            );
        }

        @Override
        protected void handleException(Exception e) {
            e.printStackTrace(System.err);
            fail(e.getLocalizedMessage());
        }

        @Override
        protected Collection<Document> compute() throws Exception {
            Thread.sleep(500);
            Collection list = new ArrayList();
            for (int i = 0; i < 10; i++)
                list.add(generateDummyDocument());
            return list;
        }

    }

    private static Document generateDummyDocument() {
        Document document = new Document();
        document.put("id", UUID.randomUUID().toString());
        document.put("value", ThreadLocalRandom.current().nextInt());
        return document;
    }

    @BeforeClass
    public static void beforeClass() {
        batchView = Guice.createInjector(new TestModule()).getInstance(TestIncrementalMongoBatchView.class);
        try {
            final ExecutorService pool = Executors.newSingleThreadExecutor();
            pool.submit(batchView);
            Thread.sleep(2000);
            batchView.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void a_fetch() throws Exception {
        Collection<Document> collection = batchView.fetch();
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        for (Document document : collection) {
            assertNotNull(document);
            assertNotNull(document.get("id"));
            assertNotNull(document.get("value"));
            assertTrue(document.get("value") instanceof Integer);
        }
    }

    @Test
    public void b_forEach() throws Exception {
        final Collection<Document> all = batchView.fetch();
        final CountDownLatch lock = new CountDownLatch(all.size());
        batchView.forEach(document -> {
            lock.countDown();
        });

        lock.await(5000, TimeUnit.SECONDS);

        assertEquals(lock.getCount(), 0);
    }

    @Test
    public void c_drop() throws Exception {
        batchView.drop();
        assertEquals(batchView.fetch().size(), 0);
    }


}