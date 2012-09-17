/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.services.web.support;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * RegisteredServiceValidator ensures that a new RegisteredService does not have
 * a conflicting Service Id with another service already in the registry.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class RegisteredServiceValidator implements Validator {

    /** Default length, which matches what is in the view. */
    private static final int DEFAULT_MAX_DESCRIPTION_LENGTH = 300;

    /** {@link ServicesManager} to look up services. */
    @NotNull
    private ServicesManager servicesManager;

    /** The maximum length of the description we will accept. */
    @Min(0)
    private int maxDescriptionLength = DEFAULT_MAX_DESCRIPTION_LENGTH;

    /** {@link IPersonAttributeDao} to manage person attributes */
    @NotNull
    private IPersonAttributeDao personAttributeDao;

    /**
     * Supports {@link RegisteredService} objects.
     * 
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(final Class<?> clazz) {
        return RegisteredService.class.isAssignableFrom(clazz);
    }

    public void validate(final Object o, final Errors errors) {
        final RegisteredService r = (RegisteredService) o;

        if (r.getServiceId() != null) {
            for (final RegisteredService service : this.servicesManager.getAllServices()) {
                if (r.getServiceId().equals(service.getServiceId())
                    && r.getId() != service.getId()) {
                    errors.rejectValue("serviceId",
                        "registeredService.serviceId.exists", null);
                    break;
                }
            }
        }

        if (r.getDescription() != null
            && r.getDescription().length() > this.maxDescriptionLength) {
            errors.rejectValue("description",
                "registeredService.description.length", null);
        }
        
        if (!StringUtils.isBlank(r.getUsernameAttribute()) && !r.isAnonymousAccess()) {
            if (!r.isIgnoreAttributes() && !r.getAllowedAttributes().contains(r.getUsernameAttribute())) {
                errors.rejectValue("usernameAttribute", "registeredService.usernameAttribute.notAvailable",
                        "This attribute is not available for this service.");
            } else {
                Set<String> availableAttributes = this.personAttributeDao.getPossibleUserAttributeNames();
                if (availableAttributes != null) {
                    if (!availableAttributes.contains(r.getUsernameAttribute())) {
                        errors.rejectValue("usernameAttribute", "registeredService.usernameAttribute.notAvailable",
                                "This attribute is not available from configured user attribute sources.");
                    }
                }
            }
        }
    }
    
    public void setServicesManager(final ServicesManager serviceRegistry) {
        this.servicesManager = serviceRegistry;
    }

    public void setMaxDescriptionLength(final int maxLength) {
        this.maxDescriptionLength = maxLength;
    }
    
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }
}
