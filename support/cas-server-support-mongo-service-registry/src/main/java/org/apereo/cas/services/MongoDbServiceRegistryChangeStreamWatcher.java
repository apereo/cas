package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LoggingUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Watches the MongoDb service registry collection through a change stream and reloads
 * service definitions once changes settle. It starts only when a replica set is defined,
 * and stops if the server does not report one; the scheduled reload stays in place either way.
 * It is not refresh-scoped: a refresh would dispose of it and nothing would start a new instance.
 * Instead it restarts itself after a refresh, so that changed settings and connections take effect.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbServiceRegistryChangeStreamWatcher implements SmartLifecycle, ApplicationListener<RefreshScopeRefreshedEvent> {
    private static final Duration MAX_AWAIT_TIME = Duration.ofSeconds(1);

    private static final Duration RETRY_DELAY = Duration.ofSeconds(10);

    private static final int MAX_QUIET_PERIODS_DEFERRED = 10;

    /**
     * Codes that mean the stream cannot be resumed from the token held:
     * {@code InvalidResumeToken}, {@code ChangeStreamFatalError} and {@code ChangeStreamHistoryLost}.
     */
    private static final Set<Integer> RESUME_FAILURE_CODES = Set.of(260, 280, 286);

    private final ObjectProvider<MongoOperations> mongoTemplateProvider;

    private final ObjectProvider<ServicesManager> servicesManagerProvider;

    private final CasConfigurationProperties casProperties;

    private final AtomicBoolean running = new AtomicBoolean();

    private final AtomicReference<@Nullable Thread> worker = new AtomicReference<>();

    /**
     * Whether the configuration names a replica set, through the {@code replica-set} setting or the
     * connection string. A {@code mongodb+srv} connection string counts as defined, since its DNS
     * records may carry the replica set; the server is asked to confirm before a stream is opened.
     *
     * @param properties the MongoDb settings
     * @return true if a replica set is defined
     */
    public static boolean isReplicaSetDefined(final BaseMongoDbProperties properties) {
        val clientUri = properties.getClientUri();
        if (StringUtils.isNotBlank(clientUri)) {
            if (Strings.CI.startsWith(clientUri, "mongodb+srv://")) {
                return true;
            }
            try {
                return StringUtils.isNotBlank(new ConnectionString(clientUri).getRequiredReplicaSetName());
            } catch (final IllegalArgumentException e) {
                LOGGER.debug("Unable to parse MongoDb connection string: [{}]", e.getMessage());
                return false;
            }
        }
        return StringUtils.isNotBlank(properties.getReplicaSet());
    }

    @Override
    public void start() {
        val mongo = casProperties.getServiceRegistry().getMongo();
        if (!mongo.getChangeStream().isEnabled()) {
            LOGGER.debug("Change streams are disabled for the MongoDb service registry");
            return;
        }
        if (!isReplicaSetDefined(mongo)) {
            LOGGER.debug("No replica set is defined for the MongoDb service registry; change streams are not used");
            return;
        }
        if (running.compareAndSet(false, true)) {
            worker.set(Thread.ofVirtual().name("MongoDbServiceRegistryChangeStreamWatcher").start(this::watch));
        }
    }

    @Override
    public void stop() {
        running.set(false);
        val thread = worker.getAndSet(null);
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(RETRY_DELAY);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void onApplicationEvent(final RefreshScopeRefreshedEvent event) {
        LOGGER.debug("Restarting the MongoDb service registry change stream after a refresh");
        stop();
        start();
    }

    private void watch() {
        var resumeToken = (BsonDocument) null;
        var opened = false;
        var pending = false;
        var pendingSince = Instant.EPOCH;
        var lastChange = Instant.EPOCH;
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                val mongoTemplate = mongoTemplateProvider.getObject();
                if (!isReplicaSetAvailable(mongoTemplate)) {
                    LOGGER.info("MongoDb service registry is not backed by a replica set; "
                        + "changes to service definitions are picked up by the scheduled reload only");
                    running.set(false);
                    return;
                }
                val collectionName = casProperties.getServiceRegistry().getMongo().getCollection();
                try (val cursor = openChangeStream(mongoTemplate, collectionName, resumeToken)) {
                    if (opened && resumeToken == null) {
                        LOGGER.debug("Change stream for [{}] could not be resumed; reloading service definitions", collectionName);
                        pending = true;
                        pendingSince = Instant.EPOCH;
                        lastChange = Instant.EPOCH;
                    }
                    opened = true;
                    LOGGER.info("Watching MongoDb collection [{}] for changes to service definitions", collectionName);
                    while (running.get() && mongoTemplate.equals(mongoTemplateProvider.getObject())) {
                        val change = cursor.tryNext();
                        val latestResumeToken = cursor.getResumeToken();
                        if (latestResumeToken != null) {
                            resumeToken = latestResumeToken;
                        }
                        val now = Instant.now();
                        if (change != null) {
                            LOGGER.trace("Observed [{}] in [{}]", change.getOperationType(), collectionName);
                            if (!pending) {
                                pending = true;
                                pendingSince = now;
                            }
                            lastChange = now;
                            if (change.getOperationType() == OperationType.INVALIDATE) {
                                resumeToken = null;
                                break;
                            }
                        }
                        if (pending && isReloadDue(pendingSince, lastChange, now)) {
                            pending = !reload();
                            pendingSince = now;
                            lastChange = now;
                        }
                    }
                }
            } catch (final MongoException e) {
                if (resumeToken != null && RESUME_FAILURE_CODES.contains(e.getCode())) {
                    LOGGER.warn("Change stream for service definitions cannot be resumed: [{}]", e.getMessage());
                    resumeToken = null;
                } else {
                    pauseAfterFailure(e);
                }
            } catch (final Exception e) {
                pauseAfterFailure(e);
            }
        }
    }

    private static boolean isReplicaSetAvailable(final MongoOperations mongoTemplate) {
        val hello = mongoTemplate.executeCommand(new Document("hello", 1));
        return StringUtils.isNotBlank(hello.getString("setName"));
    }

    private static MongoChangeStreamCursor<ChangeStreamDocument<Document>> openChangeStream(
        final MongoOperations mongoTemplate, final String collectionName, final @Nullable BsonDocument resumeToken) {
        val pipeline = List.of(Aggregates.project(Projections.include("operationType", "documentKey")));
        var changeStream = mongoTemplate.getCollection(collectionName)
            .watch(pipeline)
            .maxAwaitTime(MAX_AWAIT_TIME.toMillis(), TimeUnit.MILLISECONDS);
        if (resumeToken != null) {
            changeStream = changeStream.resumeAfter(resumeToken);
        }
        return changeStream.cursor();
    }

    private boolean isReloadDue(final Instant pendingSince, final Instant lastChange, final Instant now) {
        val quietPeriod = Beans.newDuration(casProperties.getServiceRegistry().getMongo().getChangeStream().getQuietPeriod());
        return Duration.between(lastChange, now).compareTo(quietPeriod) >= 0
            || Duration.between(pendingSince, now).compareTo(quietPeriod.multipliedBy(MAX_QUIET_PERIODS_DEFERRED)) >= 0;
    }

    private boolean reload() {
        try {
            LOGGER.debug("Reloading service definitions after changes to the MongoDb service registry");
            servicesManagerProvider.getObject().load();
            return true;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return false;
        }
    }

    private void pauseAfterFailure(final Exception e) {
        if (running.get()) {
            LOGGER.warn("Unable to watch the MongoDb service registry for changes: [{}]; retrying in [{}]", e.getMessage(), RETRY_DELAY);
            LOGGER.debug(e.getMessage(), e);
            try {
                Thread.sleep(RETRY_DELAY);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
