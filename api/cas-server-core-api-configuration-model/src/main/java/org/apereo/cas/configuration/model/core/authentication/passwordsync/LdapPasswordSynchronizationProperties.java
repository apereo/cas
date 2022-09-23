package org.apereo.cas.configuration.model.core.authentication.passwordsync;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link LdapPasswordSynchronizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class LdapPasswordSynchronizationProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -2521286056194686825L;

    /**
     * Whether or not password sync should be enabled for this ldap instance.
     */
    private boolean enabled;

    /**
     * Name of the LDAP attribute that should
     * hold the password.
     */
    @RequiredProperty
    private String passwordAttribute = "unicodePwd";

    /**
     * If synchronization fails for any reason, (ie. password update fails
     * or user account cannot be found), control whether the failure
     * should be considered fatal.
     */
    private boolean passwordSynchronizationFailureFatal;
}
