package org.apereo.cas.acme;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.CSRBuilder;
import java.util.Optional;

/**
 * This is {@link AcmeAuthorizationExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@FunctionalInterface
@Deprecated(since = "7.3.0", forRemoval = true)
public interface AcmeAuthorizationExecutor {

    /**
     * Default challenge acme authorization.
     *
     * @return the acme authorization challenge locator
     */
    static AcmeAuthorizationExecutor defaultChallenge() {
        return auth -> auth.findChallenge(Http01Challenge.class);
    }

    /**
     * Find http challenge.
     *
     * @param auth the auth
     * @return the http 01 challenge
     */
    Optional<Http01Challenge> find(Authorization auth);

    /**
     * Execute.
     *
     * @param order   the order
     * @param builder the CSR builder
     * @return the order
     * @throws Exception the exception
     */
    default Order execute(final Order order, final CSRBuilder builder) throws Exception {
        order.execute(builder.getEncoded());
        return order;
    }
}
