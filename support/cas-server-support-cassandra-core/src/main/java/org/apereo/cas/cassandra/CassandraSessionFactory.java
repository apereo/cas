package org.apereo.cas.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.FallthroughRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * This is {@link CassandraSessionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface CassandraSessionFactory {
    /**
     * Max time-to-live value in seconds (10 years).
     */
    int MAX_TTL = 10 * 365 * 24 * 60 * 60;

    /**
     * Gets session.
     *
     * @return the session
     */
    Session getSession();

    /**
     * The enum Retry policy type.
     */
    enum RetryPolicyType {
        /**
         * Default retry policy retry policy type.
         */
        DEFAULT_RETRY_POLICY(DefaultRetryPolicy.INSTANCE),
        /**
         * Downgrading consistency retry policy retry policy type.
         */
        DOWNGRADING_CONSISTENCY_RETRY_POLICY(DowngradingConsistencyRetryPolicy.INSTANCE),
        /**
         * Fallthrough retry policy retry policy type.
         */
        FALLTHROUGH_RETRY_POLICY(FallthroughRetryPolicy.INSTANCE);

        /**
         * The Retry policy.
         */
        @SuppressWarnings("ImmutableEnumChecker")
        private final RetryPolicy retryPolicy;

        /**
         * Instantiates a new Retry policy type.
         *
         * @param retryPolicy the retry policy
         */
        RetryPolicyType(final RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
        }

        /**
         * Gets retry policy.
         *
         * @return the retry policy
         */
        public RetryPolicy getRetryPolicy() {
            return this.retryPolicy;
        }
    }
}
