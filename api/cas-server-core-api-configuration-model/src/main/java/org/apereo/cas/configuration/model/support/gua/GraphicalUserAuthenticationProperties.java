package org.apereo.cas.configuration.model.support.gua;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link GraphicalUserAuthenticationProperties}
 * that contains settings needed for identification
 * of users graphically prior to executing primary authn.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-gua")
@Getter
@Setter
@Accessors(chain = true)
public class GraphicalUserAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 7527953699378415460L;

    /**
     * Locate GUA settings and images from LDAP.
     */
    private Ldap ldap = new Ldap();

    /**
     * Locate GUA settings and images from a static image.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties resource = new SpringResourceProperties();

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-support-gua")
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = 4666838063728336692L;

        /**
         * Entry attribute that holds the user image.
         */
        @RequiredProperty
        private String imageAttribute;
    }
}
