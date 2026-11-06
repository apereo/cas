package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.support.Beans;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Bounds what resolving entities of the federation can cost (OpenID Federation 1.0, section 18.1): at most a configured
 * number of resolutions run at the same time, and each may send at most a configured number of requests, counted
 * across everything the resolution fetches, the trust chains of the entity and of the issuers of its trust marks, the
 * historical keys of trust mark issuers and trust mark status requests alike, but not the historical keys of trust
 * anchors, which are kept for a minute, and must be done within a configured time, all of its requests
 * together. The request budget of a resolution is bound to it with a
 * scoped value, so that the retriever and the trust mark validator draw from it without passing it around; a resolution
 * started within another one shares its budget and its slot. Statements fetched within a resolution are kept for it, so
 * that the same statement reached through several paths is requested once.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcFederationResolutionLimiter {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "oidcFederationResolutionLimiter";

    private static final ScopedValue<RequestBudget> BUDGET = ScopedValue.newInstance();

    private static final Duration WAIT_FOR_RESOLUTION = Duration.ofSeconds(1);

    private static final Duration RESOLUTION_TIMEOUT = Duration.ofSeconds(10);

    private final Semaphore resolutions;

    private final int maxRequests;

    private final Duration waitForResolution;

    private final Duration resolutionTimeout;

    public OidcFederationResolutionLimiter(final OidcFederationProperties properties) {
        this(properties.getMaxConcurrentResolutions(), properties.getMaxRequestsPerResolution(), WAIT_FOR_RESOLUTION,
            Beans.newDuration(properties.getResolutionTimeout()));
    }

    public OidcFederationResolutionLimiter(final int maxConcurrentResolutions, final int maxRequests, final Duration waitForResolution) {
        this(maxConcurrentResolutions, maxRequests, waitForResolution, RESOLUTION_TIMEOUT);
    }

    public OidcFederationResolutionLimiter(final int maxConcurrentResolutions, final int maxRequests,
                                           final Duration waitForResolution, final Duration resolutionTimeout) {
        this.resolutions = new Semaphore(Math.max(1, maxConcurrentResolutions), true);
        this.maxRequests = Math.max(1, maxRequests);
        this.waitForResolution = waitForResolution;
        this.resolutionTimeout = resolutionTimeout;
    }

    /**
     * Run a resolution within the limits. A resolution started within another one runs as part of it.
     *
     * @param <T>        the result type
     * @param resolution the resolution
     * @return the result
     * @throws Exception when the resolution fails
     */
    public <T> T execute(final Resolution<T> resolution) throws Exception {
        if (BUDGET.isBound()) {
            return resolution.resolve();
        }
        if (!resolutions.tryAcquire(waitForResolution.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new OidcFederationResolutionBusyException("Too many federation resolutions are running; try again later");
        }
        try {
            val budget = new RequestBudget(maxRequests, Instant.now(Clock.systemUTC()).plus(resolutionTimeout));
            return ScopedValue.where(BUDGET, budget).call(resolution::resolve);
        } finally {
            resolutions.release();
        }
    }

    /**
     * The request budget of the running resolution, if any.
     *
     * @return the budget
     */
    public static Optional<RequestBudget> currentBudget() {
        return BUDGET.isBound() ? Optional.of(BUDGET.get()) : Optional.empty();
    }

    /**
     * Count a request that the running resolution, if any, is about to send.
     *
     * @param request what is requested, for the logs and the error
     * @throws IllegalStateException when the resolution has used up its budget
     */
    public static void consumeRequest(final String request) {
        currentBudget().ifPresent(budget -> {
            if (!budget.tryConsume()) {
                throw new IllegalStateException("Federation resolution exceeds %s requests; %s is not requested".formatted(budget.getMaxRequests(), request));
            }
        });
    }

    /**
     * The number of requests of the running resolution, if any, that could not be sent or completed for want of time or
     * room so far, to be passed to {@link #isIndeterminateSince(int)} once a step of the resolution is done.
     *
     * @return the mark
     */
    public static int indeterminateMark() {
        return currentBudget().map(RequestBudget::getIndeterminateRequests).orElse(0);
    }

    /**
     * Whether a request of the running resolution, if any, could not be sent or completed for want of time or room since
     * the mark was taken, so that a step that failed meanwhile could not tell.
     *
     * @param mark the mark taken when the step started
     * @return true or false
     */
    public static boolean isIndeterminateSince(final int mark) {
        return currentBudget().filter(budget -> budget.getIndeterminateRequests() > mark).isPresent();
    }

    /**
     * How many times the entity was found unreachable by the running resolution, if any, so far, to be passed to
     * {@link #isUnreachableSince(EntityID, int)} once a step of the resolution is done.
     *
     * @param entityId the entity identifier
     * @return the mark
     */
    public static int unreachableMark(final EntityID entityId) {
        return currentBudget().map(budget -> budget.getUnreachableCount(entityId.getValue())).orElse(0);
    }

    /**
     * Whether the running resolution, if any, found the entity unreachable since the mark was taken, so that a step that
     * failed meanwhile because of it could not tell; an entity found unreachable by an earlier step, and reached since,
     * tells nothing about this one.
     *
     * @param entityId the entity identifier
     * @param mark     the mark taken when the step started
     * @return true or false
     */
    public static boolean isUnreachableSince(final EntityID entityId, final int mark) {
        return currentBudget().filter(budget -> budget.getUnreachableCount(entityId.getValue()) > mark).isPresent();
    }

    /**
     * A resolution.
     *
     * @param <T> the result type
     */
    @FunctionalInterface
    public interface Resolution<T> {
        /**
         * Resolve.
         *
         * @return the result
         * @throws Exception when the resolution fails
         */
        T resolve() throws Exception;
    }

    /**
     * The requests and the time left to a resolution, the statements it fetched, the entities it could not reach, and the
     * requests it could not send or complete because it ran out of time or found no room among the running requests. A
     * step of the resolution that failed after such a request could not tell, and is unavailable rather than failed, so
     * that what it was to resolve is not remembered as unresolved; a resolution that used up its requests failed, since
     * the statements it resolves decide how many requests it needs.
     */
    public static final class RequestBudget {
        @Getter
        private final int maxRequests;

        private final AtomicInteger remaining;

        @Getter
        private final Instant deadline;

        private final Map<String, EntityStatement> statements = new ConcurrentHashMap<>();

        private final Map<String, AtomicInteger> unreachableEntities = new ConcurrentHashMap<>();

        private final AtomicInteger indeterminateRequests = new AtomicInteger();

        RequestBudget(final int maxRequests, final Instant deadline) {
            this.maxRequests = maxRequests;
            this.remaining = new AtomicInteger(maxRequests);
            this.deadline = deadline;
        }

        /**
         * Whether the resolution has run out of time.
         *
         * @return true or false
         */
        public boolean isPastDeadline() {
            return !Instant.now(Clock.systemUTC()).isBefore(deadline);
        }

        /**
         * Remember an entity that could not be reached, for reasons other than the resolution running out of time.
         *
         * @param entityId the entity identifier
         */
        public void markUnreachable(final String entityId) {
            unreachableEntities.computeIfAbsent(entityId, _ -> new AtomicInteger()).incrementAndGet();
        }

        /**
         * How many times the entity could not be reached during this resolution.
         *
         * @param entityId the entity identifier
         * @return the number
         */
        public int getUnreachableCount(final String entityId) {
            return Optional.ofNullable(unreachableEntities.get(entityId)).map(AtomicInteger::get).orElse(0);
        }

        /**
         * Use one request of the budget.
         *
         * @return whether the budget allowed it
         */
        public boolean tryConsume() {
            val allowed = remaining.getAndUpdate(left -> Math.max(0, left - 1)) > 0;
            if (!allowed) {
                LOGGER.warn("Federation resolution has used up its [{}] requests", maxRequests);
            }
            return allowed;
        }

        /**
         * Remember that a request of this resolution could not be sent or completed for want of time or of room among the
         * running requests, which tells nothing about what was requested.
         */
        public void markIndeterminate() {
            indeterminateRequests.incrementAndGet();
        }

        /**
         * The number of requests of this resolution that could not be sent or completed for want of time or room.
         *
         * @return the number
         */
        public int getIndeterminateRequests() {
            return indeterminateRequests.get();
        }

        public int getRemainingRequests() {
            return remaining.get();
        }

        /**
         * A statement this resolution fetched already.
         *
         * @param key the statement key
         * @return the statement, or null
         */
        public @Nullable EntityStatement getStatement(final String key) {
            return statements.get(key);
        }

        /**
         * Keep a statement this resolution fetched.
         *
         * @param key       the statement key
         * @param statement the statement
         */
        public void putStatement(final String key, final EntityStatement statement) {
            statements.put(key, statement);
        }
    }
}
