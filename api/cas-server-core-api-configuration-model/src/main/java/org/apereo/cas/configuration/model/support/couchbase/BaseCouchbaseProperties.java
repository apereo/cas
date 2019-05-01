package org.apereo.cas.configuration.model.support.couchbase;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link BaseCouchbaseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-couchbase-core")
public abstract class BaseCouchbaseProperties implements Serializable {

    private static final long serialVersionUID = 6550895842866988551L;

    /**
     * Nodeset name.
     */
    @RequiredProperty
    private String nodeSet = "localhost:8091";

    /**
     * String representation of connection timeout.
     */
    private String timeout = "PT30S";

    /**
     * Password.
     */
    @RequiredProperty
    private String password = "password";

    /**
     * Bucket name.
     */
    @RequiredProperty
    private String bucket = "testbucket";
}
