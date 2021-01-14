package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("LdapConsentProperties")
public class LdapConsentProperties extends AbstractLdapSearchProperties {

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
