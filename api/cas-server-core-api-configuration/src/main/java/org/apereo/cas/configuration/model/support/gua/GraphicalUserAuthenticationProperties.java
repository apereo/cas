package org.apereo.cas.configuration.model.support.gua;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.SpringResourceProperties;

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
public class GraphicalUserAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 7527953699378415460L;
    /**
     * Locate GUA settings and images from LDAP.
     */
    private Ldap ldap = new Ldap();
    /**
     * Locate GUA settings and images from a static image.
     */
    private SpringResourceProperties resource = new SpringResourceProperties();

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public SpringResourceProperties getResource() {
        return resource;
    }

    public void setResource(final SpringResourceProperties resource) {
        this.resource = resource;
    }
    
    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = 4666838063728336692L;
        /**
         * Base DN to use for the user search.
         */
        @RequiredProperty
        private String baseDn;
        /**
         * Search filter to locate the account in LDAP.
         * Syntax is {@code cn={user}} or {@code cn={0}}
         */
        @RequiredProperty
        private String userFilter;
        /**
         * Entry attribute that holds the user image.
         */
        @RequiredProperty
        private String imageAttribute;

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getUserFilter() {
            return userFilter;
        }

        public void setUserFilter(final String userFilter) {
            this.userFilter = userFilter;
        }
        
        public String getImageAttribute() {
            return imageAttribute;
        }

        public void setImageAttribute(final String imageAttribute) {
            this.imageAttribute = imageAttribute;
        }
    }

}
