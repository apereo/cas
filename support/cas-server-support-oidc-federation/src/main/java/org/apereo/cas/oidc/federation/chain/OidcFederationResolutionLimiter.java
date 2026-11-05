package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import org.apereo.cas.configuration.support.Beans;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Bounds what resolving entities of the federation can cost (OpenID Federation 1.0, section 18.1): at most a configured
 * number of resolutions run at the same time, and each may send at most a configured number of requests, counted
 * across everything the resolution fetches, the trust chains of the entity and of the issuers of its trust marks,
 * historical keys and trust mark status requests alike, and must be done within a configured time, all of its requests
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
     * The requests and the time left to a resolution, the statements it fetched, and the entities it could not reach.
     */
    public static final class RequestBudget {
        @Getter
        private final int maxRequests;

        private final AtomicInteger remaining;

        @Getter
        private final Instant deadline;

        private final Map<String, EntityStatement> statements = new ConcurrentHashMap<>();

        private final Set<String> unreachableEntities = ConcurrentHashMap.newKeySet();

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
            unreachableEntities.add(entityId);
        }

        /**
         * Whether the entity could not be reached during this resolution.
         *
         * @param entityId the entity identifier
         * @return true or false
         */
        public boolean isUnreachable(final String entityId) {
            return unreachableEntities.contains(entityId);
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
