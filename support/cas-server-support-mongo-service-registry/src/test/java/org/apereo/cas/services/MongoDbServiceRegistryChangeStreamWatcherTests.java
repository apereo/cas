package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoDbServiceRegistryProperties;
import com.mongodb.MongoException;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import lombok.val;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.data.mongodb.core.MongoOperations;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MongoDbServiceRegistryChangeStreamWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("MongoDb")
class MongoDbServiceRegistryChangeStreamWatcherTests {

    @Test
    void verifyReplicaSetDefinition() {
        assertTrue(isReplicaSetDefined(properties -> properties.setReplicaSet("rs0")));
        assertFalse(isReplicaSetDefined(properties -> properties.setHost("localhost")));
        assertFalse(isReplicaSetDefined(properties -> properties.setClientUri("not-a-mongodb-uri")));
        assertTrue(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://host1,host2/cas?replicaSet=rs0")));
        assertFalse(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://localhost/cas")));
        assertTrue(isReplicaSetDefined(properties -> properties.setClientUri("mongodb+srv://cluster.example.org/cas")));
        assertFalse(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://localhost/cas").setReplicaSet("rs0")));
    }

    @Test
    void verifyWatcherDoesNotStartWhenDisabledOrUndefined() {
        val casProperties = new CasConfigurationProperties();
        val mongo = casProperties.getServiceRegistry().getMongo();
        val watcher = new MongoDbServiceRegistryChangeStreamWatcher(mock(ObjectProvider.class), mock(ObjectProvider.class), casProperties);

        watcher.start();
        assertFalse(watcher.isRunning());

        mongo.setReplicaSet("rs0");
        mongo.getChangeStream().setEnabled(false);
        watcher.start();
        assertFalse(watcher.isRunning());
    }

    @Test
    void verifyStandaloneServerStopsWatcher() {
        try (val fixture = new WatcherFixture()) {
            when(fixture.mongo.executeCommand(any(Document.class))).thenReturn(new Document());
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                verify(fixture.mongo).executeCommand(new Document("hello", 1));
                assertFalse(fixture.watcher.isRunning());
            });
            verify(fixture.mongo, never()).getCollection(anyString());
            verifyNoInteractions(fixture.services);
        }
    }

    /**
     * Changes that arrive within the quiet period are coalesced into one reload, which waits for the quiet period to pass.
     */
    @Test
    void verifyChangesWithinQuietPeriodAreReloadedOnce() {
        try (val fixture = new WatcherFixture("PT0.5S")) {
            val loadedAt = new AtomicReference<Instant>();
            doAnswer(_ -> {
                loadedAt.compareAndSet(null, Instant.now());
                return List.of();
            }).when(fixture.services).load();
            fixture.changes.addAll(List.of(change(OperationType.INSERT), change(OperationType.UPDATE), change(OperationType.DELETE)));
            val startedAt = Instant.now();
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).until(() -> loadedAt.get() != null);
            await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(fixture.services).load());
            assertTrue(Duration.between(startedAt, loadedAt.get()).compareTo(Duration.ofMillis(500)) >= 0);
        }
    }

    /**
     * A steady stream of changes does not defer the reload forever: it happens once ten quiet periods have passed since
     * the first pending change.
     */
    @Test
    void verifyContinuousChangesDoNotDeferReloadIndefinitely() {
        try (val fixture = new WatcherFixture("PT0.2S")) {
            val event = change(OperationType.UPDATE);
            doAnswer(_ -> {
                Thread.sleep(50);
                return event;
            }).when(fixture.cursor).tryNext();
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(fixture.services, atLeastOnce()).load());
        }
    }

    @Test
    void verifyFailedReloadIsRetriedWithoutAnotherChange() {
        try (val fixture = new WatcherFixture()) {
            doThrow(new IllegalStateException("Temporary reload failure")).doReturn(List.of())
                .when(fixture.services).load();
            fixture.changes.add(change(OperationType.INSERT));
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(fixture.services, times(2)).load());
            fixture.watcher.stop();
            verify(fixture.stream).cursor();
            verify(fixture.cursor).close();
            verify(fixture.services, times(2)).load();
        }
    }

    @Test
    void verifyInvalidationReopensAndReloadsWithoutResumeToken() {
        try (val fixture = new WatcherFixture()) {
            val replacement = fixture.newCursor();
            when(fixture.stream.cursor()).thenReturn(fixture.cursor, replacement);
            when(fixture.cursor.getResumeToken()).thenReturn(BsonDocument.parse("{token: 1}"));
            fixture.changes.add(change(OperationType.INVALIDATE));
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(fixture.services).load());
            fixture.watcher.stop();
            verify(fixture.stream, times(2)).cursor();
            verify(fixture.stream, never()).resumeAfter(any(BsonDocument.class));
            verify(fixture.cursor).close();
            verify(replacement).close();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {260, 280, 286})
    void verifyRejectedResumeTokenReopensAndReloads(final int code) {
        try (val fixture = new WatcherFixture()) {
            val replacement = fixture.newCursor();
            val event = change(OperationType.INSERT);
            when(fixture.stream.cursor()).thenReturn(fixture.cursor, replacement);
            doReturn(event).doThrow(new MongoException(code, "Resume token rejected"))
                .when(fixture.cursor).tryNext();
            when(fixture.cursor.getResumeToken()).thenReturn(BsonDocument.parse("{token: 1}"));
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(fixture.services, times(2)).load());
            fixture.watcher.stop();
            verify(fixture.stream, times(2)).cursor();
            verify(fixture.stream, never()).resumeAfter(any(BsonDocument.class));
            verify(fixture.cursor).close();
            verify(replacement).close();
        }
    }

    @Test
    void verifyReplacementTemplateResumesFromLastToken() {
        try (val fixture = new WatcherFixture()) {
            val replacement = fixture.newCursor();
            val replacementMongo = mock(MongoOperations.class);
            when(replacementMongo.executeCommand(any(Document.class))).thenReturn(new Document("setName", "rs0"));
            when(replacementMongo.getCollection(anyString())).thenReturn(fixture.collection);
            val token = BsonDocument.parse("{token: 2}");
            val event = change(OperationType.UPDATE);
            when(fixture.stream.cursor()).thenReturn(fixture.cursor, replacement);
            when(fixture.cursor.getResumeToken()).thenReturn(token);
            doAnswer(_ -> {
                fixture.currentMongo.set(replacementMongo);
                return event;
            }).when(fixture.cursor).tryNext();
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(replacement, atLeastOnce()).tryNext());
            fixture.watcher.stop();
            verify(fixture.stream).resumeAfter(token);
            verify(fixture.services).load();
            verify(fixture.cursor).close();
            verify(replacement).close();
        }
    }

    @Test
    void verifyRefreshRestartsWatcherAndHonorsDisabledSetting() {
        try (val fixture = new WatcherFixture()) {
            val replacement = fixture.newCursor();
            when(fixture.stream.cursor()).thenReturn(fixture.cursor, replacement);
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(fixture.cursor, atLeastOnce()).tryNext());
            fixture.watcher.start();
            verify(fixture.stream).cursor();
            fixture.watcher.onApplicationEvent(new RefreshScopeRefreshedEvent());
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(replacement, atLeastOnce()).tryNext());
            verify(fixture.cursor).close();
            assertTrue(fixture.watcher.isRunning());
            fixture.properties.getServiceRegistry().getMongo().getChangeStream().setEnabled(false);
            fixture.watcher.onApplicationEvent(new RefreshScopeRefreshedEvent());
            assertFalse(fixture.watcher.isRunning());
            verify(fixture.stream, times(2)).cursor();
            verify(replacement).close();
            verifyNoInteractions(fixture.services);
        }
    }

    @Test
    void verifyStopInterruptsFailureBackoff() {
        try (val fixture = new WatcherFixture()) {
            val worker = new AtomicReference<Thread>();
            doAnswer(_ -> {
                worker.set(Thread.currentThread());
                throw new MongoException("Connection lost");
            }).when(fixture.cursor).tryNext();
            fixture.watcher.start();
            await().atMost(Duration.ofSeconds(5)).until(() -> worker.get() != null
                && worker.get().getState() == Thread.State.TIMED_WAITING);
            assertTimeout(Duration.ofSeconds(3), () -> {
                fixture.watcher.stop();
            });
            assertFalse(fixture.watcher.isRunning());
            assertFalse(worker.get().isAlive());
            verify(fixture.cursor).close();
            verifyNoInteractions(fixture.services);
        }
    }

    @SuppressWarnings("unchecked")
    private static ChangeStreamDocument<Document> change(final OperationType type) {
        val event = (ChangeStreamDocument<Document>) mock(ChangeStreamDocument.class);
        when(event.getOperationType()).thenReturn(type);
        return event;
    }

    private static boolean isReplicaSetDefined(final Consumer<MongoDbServiceRegistryProperties> configurer) {
        val properties = new MongoDbServiceRegistryProperties();
        configurer.accept(properties);
        return MongoDbServiceRegistryChangeStreamWatcher.isReplicaSetDefined(properties);
    }

    @SuppressWarnings("unchecked")
    private static final class WatcherFixture implements AutoCloseable {
        private final CasConfigurationProperties properties = new CasConfigurationProperties();

        private final MongoOperations mongo = mock(MongoOperations.class);

        private final ServicesManager services = mock(ServicesManager.class);

        private final MongoCollection<Document> collection = mock(MongoCollection.class);

        private final ChangeStreamIterable<Document> stream = mock(ChangeStreamIterable.class, RETURNS_SELF);

        private final BlockingQueue<ChangeStreamDocument<Document>> changes = new LinkedBlockingQueue<>();

        private final AtomicReference<MongoOperations> currentMongo = new AtomicReference<>(mongo);

        private final MongoChangeStreamCursor<ChangeStreamDocument<Document>> cursor = newCursor();

        private final MongoDbServiceRegistryChangeStreamWatcher watcher;

        private WatcherFixture() {
            this("PT0S");
        }

        private WatcherFixture(final String quietPeriod) {
            properties.getServiceRegistry().getMongo().setReplicaSet("rs0");
            properties.getServiceRegistry().getMongo().getChangeStream().setEnabled(true).setQuietPeriod(quietPeriod);
            when(mongo.executeCommand(any(Document.class))).thenReturn(new Document("setName", "rs0"));
            when(mongo.getCollection(anyString())).thenReturn(collection);
            when(collection.watch(anyList())).thenReturn(stream);
            when(stream.cursor()).thenReturn(cursor);
            val mongoProvider = (ObjectProvider<MongoOperations>) mock(ObjectProvider.class);
            val servicesProvider = (ObjectProvider<ServicesManager>) mock(ObjectProvider.class);
            when(mongoProvider.getObject()).thenAnswer(_ -> currentMongo.get());
            when(servicesProvider.getObject()).thenReturn(services);
            watcher = new MongoDbServiceRegistryChangeStreamWatcher(mongoProvider, servicesProvider, properties);
        }

        private MongoChangeStreamCursor<ChangeStreamDocument<Document>> newCursor() {
            val result = (MongoChangeStreamCursor<ChangeStreamDocument<Document>>) mock(MongoChangeStreamCursor.class);
            when(result.tryNext()).thenAnswer(_ -> changes.poll(100, TimeUnit.MILLISECONDS));
            return result;
        }

        @Override
        public void close() {
            watcher.stop();
        }
    }
}
