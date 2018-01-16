package org.apereo.cas.configuration.model.support.couchbase;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public abstract class BaseCouchbaseProperties implements Serializable {

    private static final long serialVersionUID = 6550895842866988551L;

    /**
     * Flag to indicate if query is enabled.
     */
    private boolean queryEnabled = true;

    /**
     * Nodeset name.
     */
    @RequiredProperty
    private String nodeSet = "localhost:8091";

    /**
     * String representation of connection timeout.
     */
    private String timeout = "PT10S";

    /**
     * Password.
     */
    @RequiredProperty
    private String password;

    /**
     * Bucket name.
     */
    @RequiredProperty
    private String bucket = "default";
}
