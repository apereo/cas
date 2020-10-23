package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LdapGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-gauth-ldap")
@Accessors(chain = true)
public class LdapGoogleAuthenticatorMultifactorProperties extends AbstractLdapSearchProperties {
    private static final long serialVersionUID = -100556119517414696L;

    /**
     * Name of LDAP attribute that holds GAuth account/credential as JSON.
     */
    private String accountAttributeName = "casGAuthRecord";
}

