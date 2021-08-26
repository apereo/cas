package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jAppleOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jAppleOidcClientProperties")
public class Pac4jAppleOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 2258382317533639638L;

    /**
     * Client secret expiration timeout.
     */
    @DurationCapable
    private String timeout = "PT30S";

    /**
     * The identifier for the private key.
     * Usually the 10 character Key ID of the private key you create
     * in Apple.
     */
    private String privateKeyId;

    /**
     * Apple team identifier.
     * Usually, 10 character string given to you by Apple.
     */
    private String teamId;

    /**
     * Private key obtained from Apple.
     * Must point to a resource that resolved to an elliptic curve (EC) private key.
     */
    private String privateKey;
}
