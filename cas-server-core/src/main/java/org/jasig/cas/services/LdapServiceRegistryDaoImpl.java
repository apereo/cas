/**
 * Copyright (c) 2007, The Regents of the University of California (Regents)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the name of the The Regents of the University of California (Regents) nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.jasig.cas.services;

import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.simple.SimpleLdapTemplate;
import org.springframework.util.StringUtils;

import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;

/**
 * Implementation of the ServiceRegistryDao based on LdapTemplate.
 * 
 * @author Lucas Rockwell
 * @author Scott Battaglia
 * @version $Revision: $ $Date: $
 * @since 3.1.2
 */

public final class LdapServiceRegistryDaoImpl implements ServiceRegistryDao {

    /** String representing the base of the Service Registry in LDAP. */
    private static final String SERVICE_REGISTRY_BASE = "ou=service registry,ou=cas,ou=services";
    
	/** LdapTemplate that we use for managing the LDAP data. */
    @NotNull
	private SimpleLdapTemplate ldapTemplate;
	
	/** Log object for logging. */
    private Log log = LogFactory.getLog(this.getClass());
    
    public RegisteredService save(final RegisteredService registeredService) {
    	final boolean isNew = registeredService.getId() == -1;
		final DistinguishedName serviceDn = new DistinguishedName(SERVICE_REGISTRY_BASE);
    	final Attributes serviceAttributes = new BasicAttributes();
    	
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
    	serviceAttributes.put("berkeleyEduCasServiceEnabled", Boolean.toString(registeredService.isEnabled()));
    	serviceAttributes.put("berkeleyEduCasServiceSsoEnabled", Boolean.toString(registeredService.isSsoEnabled()));
    	serviceAttributes.put("berkeleyEduCasServiceAnnonymousAccess", Boolean.toString(registeredService.isAnonymousAccess()));
    	serviceAttributes.put("berkeleyEduCasServiceUrl", registeredService.getServiceId());
    	
    	if (isNew) {
    		String id = new Long(Math.abs((new Random()).nextInt() / 1000)).toString();
    		serviceAttributes.put("uid", id);
    		serviceDn.add("uid", id);
    		this.ldapTemplate.bind(serviceDn.encode(), null, serviceAttributes);
    		((RegisteredServiceImpl) registeredService).setId(Long.parseLong(id));
    	}
    	else {
    	    final String uid = Long.toString(registeredService.getId());
        	serviceAttributes.put("uid", uid);
        	serviceDn.add("uid", uid);
//        	this.ldapTemplate.rebind(serviceDn, null, serviceAttributes);
        	// XXX use the modifyAttributes value
    	}
    	
    	return registeredService;
    }

    /**
     * Remove the service from the data store.
     * 
     * @param registeredService the service to remove.
     * @return true if it was removed, false otherwise.
     */
    public boolean delete(final RegisteredService registeredService) {
    	try {
    		this.ldapTemplate.unbind("uid=" + Long.toString(registeredService.getId()) + ","+SERVICE_REGISTRY_BASE);
    		return true;
    	}
    	catch (final DataAccessException dae) {
    			log.error("Could not delete the Service entry: " + registeredService.getName(), dae);
    		return false;
    	}
    }

    /**
     * Retrieve the services from the data store.
     * 
     * @return List of RegisteredService objects
     */
    public List<RegisteredService> load() {
    	return this.ldapTemplate.search(SERVICE_REGISTRY_BASE, "(objectclass=berkeleyEduCasServiceRegistration)", new RegisteredServiceParameterizedContextMapper());
    }

    /**
     * Retrieves a service that matched the passed in id.
     * 
     * @param id
     * @return RegisteredService
     */
    public RegisteredService findServiceById(final long id) {
    	return this.ldapTemplate.lookup("uid=" + id + ",objectclass=berkeleyEduCasServiceRegistration,"+SERVICE_REGISTRY_BASE, new RegisteredServiceParameterizedContextMapper());
    }
    
    /**
     * Spring sets the ldapTemplate via the xml config.
     */
    public void setLdapTemplate(final SimpleLdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }
}