package org.apereo.cas.couchbase.core;

/**
 * This is {@link CouchbaseException}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
public class CouchbaseException extends RuntimeException {
    private static final long serialVersionUID = -388018477671208990L;

    public CouchbaseException(final String message) {
        super(message);
    }
}
