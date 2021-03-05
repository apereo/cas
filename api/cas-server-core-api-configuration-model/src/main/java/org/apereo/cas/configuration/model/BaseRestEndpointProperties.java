package org.apereo.cas.configuration.model;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link BaseRestEndpointProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class BaseRestEndpointProperties implements Serializable {
    private static final long serialVersionUID = 2687020856160473089L;

    /**
     * The endpoint URL to contact and retrieve attributes.
     */
    @RequiredProperty
    private String url;

    /**
     * If REST endpoint is protected via basic authentication,
     * specify the username for authentication.
     */
    private String basicAuthUsername;

    /**
     * If REST endpoint is protected via basic authentication,
     * specify the password for authentication.
     */
    private String basicAuthPassword;

    /**
     * Headers, defined as a Map, to include in the request when making the REST call.
     * Will overwrite any header that CAS is pre-defined to
     * send and include in the request. Key in the map should be the header name
     * and the value in the map should be the header value.
     */
    private Map<String, String> headers = new HashMap<>();
}
