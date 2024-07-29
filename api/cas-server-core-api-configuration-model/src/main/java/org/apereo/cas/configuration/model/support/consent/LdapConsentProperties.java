package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link LdapConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-consent-ldap")
@Getter
@Setter
@Accessors(chain = true)

public class LdapConsentProperties extends AbstractLdapSearchProperties {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Type of LDAP directory.
     */
    @RequiredProperty
    private LdapType type;

    /**
     * Name of LDAP attribute that holds consent decisions as JSON.
     */
    @RequiredProperty
    private String consentAttributeName = "casConsentDecision";
}
