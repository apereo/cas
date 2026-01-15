package org.apereo.cas.configuration.model.support.pac4j;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link Pac4jDelegatedAuthenticationProfileSelectionProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationProfileSelectionProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1478567744591488495L;

    /**
     * Groovy script to execute operations on profile selection.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovy = new SpringResourceProperties();

    /**
     * Connect to LDAP servers to locate candidate profiles for delegated authn.
     */
    private List<Pac4jDelegatedAuthenticationLdapProfileSelectionProperties> ldap = new ArrayList<>();
}
