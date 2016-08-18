package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.services.RegisteredService;
import org.ldaptive.LdapEntry;

/**
 * Strategy interface to define operations required when mapping LDAP
 * entries to registered services and vice versa.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @see DefaultLdapRegisteredServiceMapper
 * @since 3.0.0
 */
public interface LdapRegisteredServiceMapper {

    /**
     * Map to registered service from ldap.
     *
     * @param result the result
     * @return the registered service
     */
    RegisteredService mapToRegisteredService(LdapEntry result);

    /**
     * Map from registered service to ldap.
     *
     * @param dn the dn
     * @param svc the svc
     * @return the ldap entry
     */
    LdapEntry mapFromRegisteredService(String dn, RegisteredService svc);

    /**
     * Gets the dn for registered service.
     *
     * @param parentDn the parent dn
     * @param svc the svc
     * @return the dn for registered service
     */
    String getDnForRegisteredService(String parentDn, RegisteredService svc);

    /**
     * Gets the name of the LDAP object class that represents service registry entries.
     *
     * @return Registered service object class.
     */
    String getObjectClass();

    /**
     * Gets the name of the LDAP attribute that stores the registered service integer unique identifier.
     *
     * @return Registered service unique ID attribute name.
     */
    String getIdAttribute();
}
