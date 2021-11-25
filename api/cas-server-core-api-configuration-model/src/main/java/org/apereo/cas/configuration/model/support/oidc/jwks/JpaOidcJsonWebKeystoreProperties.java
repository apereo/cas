package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JpaOidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("JpaOidcJsonWebKeystoreProperties")
public class JpaOidcJsonWebKeystoreProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = 1633689616653363554L;

    /**
     * Ensure the URL is set to null
     * to then conditionally activate components
     * based on defined URL values in settings.
     */
    public JpaOidcJsonWebKeystoreProperties() {
        setUrl(null);
    }
}
