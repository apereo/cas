package org.apereo.cas.configuration.model.support.ldap.serviceregistry;

/**
 * This is {@link LdapServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class LdapServiceRegistryProperties {
    
    private String baseDn = "dc=example,dc=org";
    private String objectClass = "casRegisteredService";
    private String idAttribute = "uid";
    private String serviceDefinitionAttribute = "description";

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(final String objectClass) {
        this.objectClass = objectClass;
    }

    public String getIdAttribute() {
        return idAttribute;
    }

    public void setIdAttribute(final String idAttribute) {
        this.idAttribute = idAttribute;
    }

    public String getServiceDefinitionAttribute() {
        return serviceDefinitionAttribute;
    }

    public void setServiceDefinitionAttribute(final String serviceDefinitionAttribute) {
        this.serviceDefinitionAttribute = serviceDefinitionAttribute;
    }
}



