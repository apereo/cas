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

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
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

    @Value("${cas.rest.services.attributename:")
    private String attributeName;

    @Value("${cas.rest.services.attributevalue:")
    private String attributeValue;

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/v1/services/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                            @PathVariable("tgtId") final String tgtId) {
        try {

            if (StringUtils.isBlank(this.attributeName) || StringUtils.isBlank(this.attributeValue)) {
                throw new IllegalArgumentException("Attribute name and/or value must be configured");
            }

            final TicketGrantingTicket ticket =
                this.centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            final Map<String, Object> attributes = ticket.getAuthentication().getPrincipal().getAttributes();
            final Map<String, Set<String>> requiredAttrs = new HashMap<>();
            requiredAttrs.put(this.attributeName, Collections.singleton(this.attributeValue));

            if (attributes.containsKey(this.attributeName)) {
                final Set requiredValues = new HashSet(requiredAttrs.values());
                final Set presentValues = new HashSet(attributes.values());

                final Sets.SetView<String> difference = Sets.intersection(requiredValues, presentValues);
                final Set<String> copy = difference.immutableCopy();

                if (!copy.isEmpty()) {
                    final String serviceId = requestBody.getFirst("serviceId");
                    final String serviceName = requestBody.getFirst("name");
                    final String description = requestBody.getFirst("description");
                    final String evaluationOrder = requestBody.getFirst("evaluationOrder");

                    if (StringUtils.isBlank(serviceId) || StringUtils.isBlank(serviceName)
                        || StringUtils.isBlank(description)) {
                        throw new IllegalArgumentException("Service name/description/id is missing");
                    }

                    final RegexRegisteredService service = new RegexRegisteredService();
                    service.setServiceId(serviceId);
                    service.setDescription(description);
                    service.setName(serviceName);
                    service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));

                    if (StringUtils.isBlank(evaluationOrder)) {
                        service.setEvaluationOrder(Integer.MAX_VALUE);
                    } else {
                        service.setEvaluationOrder(Integer.parseInt(evaluationOrder));
                    }

                    this.servicesManager.save(service);
                    return new ResponseEntity<>(String.valueOf(service.getId()), HttpStatus.OK);
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

}
