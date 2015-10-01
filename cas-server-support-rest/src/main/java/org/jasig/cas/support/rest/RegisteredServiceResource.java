/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.support.rest;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link org.springframework.web.bind.annotation.RestController} implementation of a REST API
 * that allows for registration of CAS services. Services will automatically be put in in the service registry,
 * but are always disabled by default until they are approved by the explicit permission of the CAS deployer.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RestController()
public class RegisteredServiceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceResource.class);

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Value("${cas.rest.services.attributename:}")
    private String attributeName;

    @Value("${cas.rest.services.attributevalue:}")
    private String attributeValue;

    /**
     * Create new service.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/services/add/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createService(@RequestBody final MultiValueMap<String, String> requestBody,
                                                      @PathVariable("tgtId") final String tgtId) {
        try {

            if (StringUtils.isBlank(this.attributeName) || StringUtils.isBlank(this.attributeValue)) {
                throw new IllegalArgumentException("Attribute name and/or value must be configured");
            }

            final TicketGrantingTicket ticket =
                this.centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            if (ticket == null || ticket.isExpired()) {
                throw new InvalidTicketException("Ticket-granting ticket " + tgtId + " is not found");
            }
            final Map<String, Object> attributes = ticket.getAuthentication().getPrincipal().getAttributes();
            final Map<String, Set<String>> requiredAttrs = new HashMap<>();
            requiredAttrs.put(this.attributeName, Collections.singleton(this.attributeValue));

            if (attributes.containsKey(this.attributeName)) {
                final Collection<String> attributeValuesToCompare = new HashSet<>();
                final Object value = attributes.get(this.attributeName);
                if (value instanceof Collection) {
                    attributeValuesToCompare.addAll((Collection<String>) value);
                } else {
                    attributeValuesToCompare.add(value.toString());
                }

                if (attributeValuesToCompare.contains(this.attributeValue)) {
                    final RegexRegisteredService service = parseRequestToExtractService(requestBody);
                    final RegisteredService savedService = this.servicesManager.save(service);
                    return new ResponseEntity<>(String.valueOf(savedService.getId()), HttpStatus.OK);
                }
            }
            throw new IllegalArgumentException("Request is not authorized");

        } catch (final InvalidTicketException e) {
            return new ResponseEntity<>("TicketGrantingTicket could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private RegexRegisteredService parseRequestToExtractService(@RequestBody final MultiValueMap<String, String> requestBody) {
        final String serviceId = requestBody.getFirst("serviceId");
        final String serviceName = requestBody.getFirst("name");
        final String description = requestBody.getFirst("description");
        final String evaluationOrder = requestBody.getFirst("evaluationOrder");
        final String enabled = requestBody.getFirst("enabled");
        final String ssoEnabled = requestBody.getFirst("ssoEnabled");

        if (StringUtils.isBlank(serviceId) || StringUtils.isBlank(serviceName)
            || StringUtils.isBlank(description)) {
            throw new IllegalArgumentException("Service name/description/id is missing");
        }


        final RegexRegisteredService service = new RegexRegisteredService();
        service.setServiceId(serviceId);
        service.setDescription(description);
        service.setName(serviceName);

        if (StringUtils.isBlank(evaluationOrder)) {
            service.setEvaluationOrder(Integer.MAX_VALUE);
        } else {
            service.setEvaluationOrder(Integer.parseInt(evaluationOrder));
        }

        boolean enabledService = false;
        boolean ssoEnabledService = false;
        if (!StringUtils.isBlank(enabled)) {
            enabledService = BooleanUtils.toBoolean(enabled);
        }
        if (!StringUtils.isBlank(ssoEnabled)) {
            ssoEnabledService = BooleanUtils.toBoolean(ssoEnabled);
        }

        service.setAccessStrategy(
            new DefaultRegisteredServiceAccessStrategy(enabledService, ssoEnabledService));
        return service;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeValue(final String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
