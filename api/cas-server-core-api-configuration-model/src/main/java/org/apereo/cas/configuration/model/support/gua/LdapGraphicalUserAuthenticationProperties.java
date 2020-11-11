package org.apereo.cas.configuration.model.support.gua;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LdapGraphicalUserAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-gua")
@Getter
@Setter
@Accessors(chain = true)
public class LdapGraphicalUserAuthenticationProperties extends AbstractLdapSearchProperties {

    private static final long serialVersionUID = 4666838063728336692L;

    /**
     * Entry attribute that holds the user image.
     */
    @RequiredProperty
    private String imageAttribute;
}
