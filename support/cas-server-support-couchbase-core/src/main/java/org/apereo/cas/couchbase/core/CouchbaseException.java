package org.apereo.cas.couchbase.core;

/**
 * This is {@link CouchbaseException}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
public class CouchbaseException extends RuntimeException {
    public CouchbaseException(final String message) {
        super(message);
    }
}
