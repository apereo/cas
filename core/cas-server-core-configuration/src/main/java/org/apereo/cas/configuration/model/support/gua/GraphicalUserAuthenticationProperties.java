package org.apereo.cas.configuration.model.support.gua;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;

/**
 * This is {@link GraphicalUserAuthenticationProperties}
 * that contains settings needed for identification
 * of users graphically prior to executing primary authn.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationProperties {
    private Ldap ldap = new Ldap();
    private Resource resource = new Resource();

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }

    public static class Resource {
        private String location;

        public String getLocation() {
            return location;
        }

        public void setLocation(final String location) {
            this.location = location;
        }
    }
    
    public static class Ldap extends AbstractLdapProperties {
        private String baseDn;
        private String userFilter;
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
