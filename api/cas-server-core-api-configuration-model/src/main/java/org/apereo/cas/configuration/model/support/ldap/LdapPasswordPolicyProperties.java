package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LdapPasswordPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-ldap")
@Accessors(chain = true)
public class LdapPasswordPolicyProperties extends PasswordPolicyProperties {
    private static final long serialVersionUID = -1878237508646993100L;

    /**
     * An implementation of a policy class that knows how to handle LDAP responses.
     * The class must be an implementation of {@code org.ldaptive.auth.AuthenticationResponseHandler}.
     */
    private String customPolicyClass;

    /**
     * LDAP type. Accepted values are {@code GENERIC,AD,FreeIPA,EDirectory}
     */
    private AbstractLdapProperties.LdapType type = AbstractLdapProperties.LdapType.GENERIC;

}
