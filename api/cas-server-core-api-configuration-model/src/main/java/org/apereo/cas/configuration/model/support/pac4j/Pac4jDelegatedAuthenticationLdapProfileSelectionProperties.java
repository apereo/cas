package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Pac4jDelegatedAuthenticationLdapProfileSelectionProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jDelegatedAuthenticationLdapProfileSelectionProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = 3372867394066286022L;

    /**
     * List of attributes that should be retrieved from LDAP
     * for this profile.
     */
    private List<String> attributes = new ArrayList<>();

    /**
     * User attribute from LDAP that determines the identifier
     * of the linked profile.
     */
    @Required
    private String profileIdAttribute = "uid";
}
