package org.apereo.cas.configuration.support;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
}
