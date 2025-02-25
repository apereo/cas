package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link BaseSyncopeSearchProperties}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseSyncopeSearchProperties extends BaseSyncopeProperties {

    @Serial
    private static final long serialVersionUID = 18257222412164L;

    /**
     * User FIQL filter to use for searching.
     * Syntax is {@code username=={user}} or {@code username=={0}}.
     */
    @RequiredProperty
    protected String searchFilter = "username=={user}";

    /**
     * Specify the username for REST authentication.
     */
    @RequiredProperty
    private String basicAuthUsername;

    /**
     * Specify the password for REST authentication.
     */
    @RequiredProperty
    private String basicAuthPassword;

    /**
     * Headers, defined as a Map, to include in the request when making the REST call.
     * Will overwrite any header that CAS is pre-defined to
     * send and include in the request. Key in the map should be the header name
     * and the value in the map should be the header value.
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * Map of attributes that optionally may be used to control the names
     * of the collected attributes from Syncope. If an attribute is provided by Syncope,
     * it can be listed here as the key of the map with a value that should be the name
     * of that attribute as collected and recorded by CAS.
     * For example, the convention {@code lastLoginDate->lastDate} will process the
     * Syncope attribute {@code lastLoginDate} and will internally rename that to {@code lastDate}.
     * If no mapping is specified, CAS defaults will be used instead.
     */
    private Map<String, String> attributeMappings = new LinkedHashMap<>();

}
