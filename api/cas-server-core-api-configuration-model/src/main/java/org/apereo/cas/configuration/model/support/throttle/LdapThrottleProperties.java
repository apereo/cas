package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link LdapThrottleProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-throttle-ldap")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("LdapThrottleProperties")
public class LdapThrottleProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = 7519847618333749780L;

    /**
     * Name of LDAP attribute that represents the account locked status.
     * The value of the attribute is set to {@code "true"} if the account is
     * ever updated to indicated a locked status.
     */
    @RequiredProperty
    private String accountLockedAttribute = "pwdLockout";
}
