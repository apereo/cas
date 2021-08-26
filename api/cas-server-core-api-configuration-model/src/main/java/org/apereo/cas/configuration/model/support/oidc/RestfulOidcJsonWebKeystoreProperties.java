package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulOidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class RestfulOidcJsonWebKeystoreProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 3659099897056632608L;
}
