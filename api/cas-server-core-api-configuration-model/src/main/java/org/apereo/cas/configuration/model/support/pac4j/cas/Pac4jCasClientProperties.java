package org.apereo.cas.configuration.model.support.pac4j.cas;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jCasClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jCasClientProperties extends Pac4jBaseClientProperties {

    private static final long serialVersionUID = -2738631545437677447L;

    /**
     * The CAS server login url.
     */
    @RequiredProperty
    private String loginUrl;

    /**
     * CAS protocol to use.
     * Acceptable values are {@code CAS10, CAS20, CAS20_PROXY, CAS30, CAS30_PROXY, SAML}.
     */
    @RequiredProperty
    private String protocol = "CAS20";

    public Pac4jCasClientProperties() {
        setCallbackUrlType(CallbackUrlTypes.PATH_PARAMETER);
    }
}
