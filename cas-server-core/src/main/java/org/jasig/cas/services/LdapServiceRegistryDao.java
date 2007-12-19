/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;
import java.util.Random;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class LdapServiceRegistryDao implements ServiceRegistryDao {
        
    /** String representing the base of the Service Registry in LDAP. */
    private final static String SERVICE_REGISTRY_BASE =
        "ou=service registry,ou=cas,ou=services";
    
    /** Simple LDAP Template instance. */
    @NotNull
    private final SimpleLdapTemplate ldapTemplate;
    
    /** Log object for logging. */
    protected Log log = LogFactory.getLog(this.getClass());

    public LdapServiceRegistryDao(final SimpleLdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public boolean delete(final RegisteredService registeredService) {
        final DistinguishedName serviceDn = new DistinguishedName(SERVICE_REGISTRY_BASE);
        serviceDn.add("uid", Long.toString(registeredService.getId()));
        this.ldapTemplate.unbind(serviceDn.encode());
        return true;
    }

    public RegisteredService findServiceById(final long id) {
        final AndFilter andFilter = new AndFilter().and(new EqualsFilter("objectclass", "berkeleyEduCasServiceRegistration")).and(new EqualsFilter("uid", Long.toString(id)));        
        return this.ldapTemplate.lookup(andFilter.encode(), new RegisteredServiceParameterizedContextMapper());
    }

    public List<RegisteredService> load() {
        return this.ldapTemplate.search(SERVICE_REGISTRY_BASE, "(objectclass=berkeleyEduCasServiceRegistration)", new RegisteredServiceParameterizedContextMapper());
    }

    public RegisteredService save(final RegisteredService registeredService) {
        final boolean isNew = registeredService.getId() == -1;
        final DistinguishedName serviceDn = new DistinguishedName(SERVICE_REGISTRY_BASE);
        final  Attributes serviceAttributes = new BasicAttributes();
        final BasicAttribute objectClassAttribute = new BasicAttribute("objectclass");
        objectClassAttribute.add("top");
        objectClassAttribute.add("berkeleyEduCasServiceRegistration");
        serviceAttributes.put(objectClassAttribute);
        
        if (registeredService.getAllowedAttributes().length > 0) {
            final BasicAttribute allowedAttributesAttribute = new BasicAttribute("berkeleyEduCasServiceAllowedAttributes");

            for (final String attr : registeredService.getAllowedAttributes()) {
                allowedAttributesAttribute.add(attr);
            }
            serviceAttributes.put(allowedAttributesAttribute);
        }
        
        serviceAttributes.put("cn", registeredService.getName());
        serviceAttributes.put("berkeleyEduCasServiceDescription", registeredService.getDescription());
        
        if (StringUtils.hasText(registeredService.getTheme())) {
            serviceAttributes.put("berkeleyEduCasServiceTheme", registeredService.getTheme());
        }
                
        serviceAttributes.put("berkeleyEduCasServiceAllowedToProxy", Boolean.toString(registeredService.isAllowedToProxy()));
        serviceAttributes.put("berkeleyEduCasServiceEnabled",Boolean.toString(registeredService.isEnabled()));
        serviceAttributes.put("berkeleyEduCasServiceSsoEnabled", Boolean.toString(registeredService.isSsoEnabled()));
        serviceAttributes.put("berkeleyEduCasServiceAnnonymousAccess", Boolean.toString(registeredService.isAnonymousAccess()));
        serviceAttributes.put("berkeleyEduCasServiceUrl", registeredService.getServiceId());
        
        if (isNew) {
            final long id = new Long(Math.abs((new Random()).nextInt() / 1000)).longValue();
            ((RegisteredServiceImpl) registeredService).setId(id);
        }

        serviceAttributes.put("uid", Long.toString(registeredService.getId()));
        serviceDn.add("uid",Long.toString(registeredService.getId()));
        
        if (isNew) {
            this.ldapTemplate.bind(serviceDn.encode(), null, serviceAttributes);            
        }
        else {
// XXX: figure out how to use modifyAttributes
            // final DirContextOperations ctxt = 
           // this.ldapTemplate.modifyAttributes(ctx);
            // ldapTemplate.rebind(serviceDn, null, serviceAttributes);
        }
        
        return registeredService;
    }
}
