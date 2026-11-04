package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationProperties;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatement;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Bounds what resolving entities of the federation can cost (OpenID Federation 1.0, section 18.1): at most a configured
 * number of resolutions run at the same time, and each may send at most a configured number of requests, counted
 * across everything the resolution fetches, the trust chains of the entity and of the issuers of its trust marks,
 * historical keys and trust mark status requests alike. The request budget of a resolution is bound to it with a
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

    private static final Duration WAIT_FOR_RESOLUTION = Duration.ofSeconds(5);

    private final Semaphore resolutions;

    private final int maxRequests;

    private final Duration waitForResolution;

    public OidcFederationResolutionLimiter(final OidcFederationProperties properties) {
        this(properties.getMaxConcurrentResolutions(), properties.getMaxRequestsPerResolution(), WAIT_FOR_RESOLUTION);
    }

    public OidcFederationResolutionLimiter(final int maxConcurrentResolutions, final int maxRequests, final Duration waitForResolution) {
        this.resolutions = new Semaphore(Math.max(1, maxConcurrentResolutions), true);
        this.maxRequests = Math.max(1, maxRequests);
        this.waitForResolution = waitForResolution;
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
            return ScopedValue.where(BUDGET, new RequestBudget(maxRequests)).call(resolution::resolve);
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
     * The requests left to a resolution, and the statements it fetched.
     */
    public static final class RequestBudget {
        private final int maxRequests;

        private final AtomicInteger remaining;

        private final Map<String, EntityStatement> statements = new ConcurrentHashMap<>();

        RequestBudget(final int maxRequests) {
            this.maxRequests = maxRequests;
            this.remaining = new AtomicInteger(maxRequests);
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

        public int getMaxRequests() {
            return maxRequests;
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
