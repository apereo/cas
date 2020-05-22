package org.apereo.cas.configuration.model.support.passwordless.account;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PasswordlessAuthenticationLdapAccountsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-passwordless-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationLdapAccountsProperties extends AbstractLdapSearchProperties {
    private static final long serialVersionUID = -1102345678378393382L;

    /**
     * Name of the LDAP attribute that
     * indicates the user's email address.
     */
    private String emailAttribute = "mail";

    /**
     * Name of the LDAP attribute that
     * indicates the user's phone.
     */
    private String phoneAttribute = "phoneNumber";
}
