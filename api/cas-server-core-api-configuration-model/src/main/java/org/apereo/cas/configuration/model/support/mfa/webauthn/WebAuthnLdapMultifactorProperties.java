package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WebAuthnLdapMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn-ldap")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WebAuthnLdapMultifactorProperties")
public class WebAuthnLdapMultifactorProperties extends AbstractLdapSearchProperties {
    private static final long serialVersionUID = -1161683393319585262L;

    /**
     * Name of LDAP attribute that holds WebAuthn account/credential as JSON.
     */
    @RequiredProperty
    private String accountAttributeName = "casWebAuthnRecord";
}

