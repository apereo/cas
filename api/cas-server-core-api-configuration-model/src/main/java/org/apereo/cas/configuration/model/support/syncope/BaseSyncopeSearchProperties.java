package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.HashMap;
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
@JsonFilter("BaseSyncopeSearchProperties")
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

}
