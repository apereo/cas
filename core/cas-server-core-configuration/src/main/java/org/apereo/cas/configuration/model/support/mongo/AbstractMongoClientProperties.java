package org.apereo.cas.configuration.model.support.mongo;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link AbstractMongoClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractMongoClientProperties implements Serializable {
    private static final long serialVersionUID = 2844298699021872943L;

    /**
     * The connection uri to the mongodb instance.
     * This typically takes on the form of {@code mongodb://user:psw@ds135522.somewhere.com:35522/db}
     */
    private String clientUri = StringUtils.EMPTY;

    /**
     * The collection name to use and create statically.
     */
    private String collection = StringUtils.EMPTY;

    /**
     * When pre-existing collections should be dropped on startup,
     * rebooting the current data before doing anything else.
     */
    private boolean dropCollection;

    public String getClientUri() {
        return clientUri;
    }

    public void setClientUri(final String clientUri) {
        this.clientUri = clientUri;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(final String collection) {
        this.collection = collection;
    }

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }
}
