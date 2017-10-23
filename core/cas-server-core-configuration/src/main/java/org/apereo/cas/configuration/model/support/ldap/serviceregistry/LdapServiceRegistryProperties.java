package org.apereo.cas.configuration.model.support.ldap.serviceregistry;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link LdapServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ldap-service-registry")
public class LdapServiceRegistryProperties extends AbstractLdapProperties {

    private static final long serialVersionUID = 2372867394066286022L;

    /**
     * Object class used for the registered service entry in LDAP.
     */
    private String objectClass = "casRegisteredService";

    /**
     * ID attribute used for the registered service entry in LDAP
     * to keep track of the service numeric identifier.
     */
    private String idAttribute = "uid";

    /**
     * Service definintion attribute used for the registered service entry in LDAP
     * to keep a representation of the service body.
     */
    private String serviceDefinitionAttribute = "description";

    /**
     * LDAP baseDn used for stored and retrieval of records from LDAP.
     */
    @RequiredProperty
    private String baseDn;

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



