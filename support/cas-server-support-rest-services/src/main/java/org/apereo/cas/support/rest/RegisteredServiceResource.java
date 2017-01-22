package org.apereo.cas.support.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * {@link RestController} implementation of a REST API
 * that allows for registration of CAS services.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RestController("registeredServiceResourceRestController")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegisteredServiceResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceResource.class);

    private final ServicesManager servicesManager;
    private final CentralAuthenticationService centralAuthenticationService;
    private final String attributeName;
    private final String attributeValue;

    public RegisteredServiceResource(final ServicesManager servicesManager, final CentralAuthenticationService centralAuthenticationService,
                                     final String attributeName, final String attributeValue) {
        this.servicesManager = servicesManager;
        this.centralAuthenticationService = centralAuthenticationService;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    /**
     * Create new service.
     *
     * @param tgtId             ticket granting ticket id URI path param
     * @param serviceDataHolder the service to register and save in rest form
     * @return {@link ResponseEntity} representing RESTful response
     */
    @PostMapping(value = "/v1/services/add/{tgtId:.+}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createService(@ModelAttribute final ServiceDataHolder serviceDataHolder, @PathVariable("tgtId") final String tgtId) {
        try {
            if (StringUtils.isBlank(this.attributeName) || StringUtils.isBlank(this.attributeValue)) {
                throw new IllegalArgumentException("Attribute name and/or value must be configured");
            }

            final TicketGrantingTicket ticket = this.centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            if (ticket == null || ticket.isExpired()) {
                throw new InvalidTicketException("Ticket-granting ticket " + tgtId + " is not found");
            }
            final Map<String, Object> attributes = ticket.getAuthentication().getPrincipal().getAttributes();
            if (attributes.containsKey(this.attributeName)) {
                final Collection<String> attributeValuesToCompare = new HashSet<>();
                final Object value = attributes.get(this.attributeName);
                if (value instanceof Collection) {
                    attributeValuesToCompare.addAll((Collection<String>) value);
                } else {
                    attributeValuesToCompare.add(value.toString());
                }

                if (attributeValuesToCompare.contains(this.attributeValue)) {
                    final RegisteredService service = serviceDataHolder.getRegisteredService();
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

    private static class ServiceDataHolder implements Serializable {

        private static final long serialVersionUID = 3035541944428412672L;

        private String serviceId;
        private String name;
        private String description;
        private int evaluationOrder = Integer.MAX_VALUE;
        private boolean enabled;
        private boolean ssoEnabled;

        public void setServiceId(final String serviceId) {
            this.serviceId = serviceId;
        }

        public void setName(final String serviceName) {
            this.name = serviceName;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setEvaluationOrder(final int evaluationOrder) {
            this.evaluationOrder = evaluationOrder;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public void setSsoEnabled(final boolean ssoEnabled) {
            this.ssoEnabled = ssoEnabled;
        }

        public RegisteredService getRegisteredService() {
            if (StringUtils.isBlank(this.serviceId) || StringUtils.isBlank(this.name)
                    || StringUtils.isBlank(this.description)) {
                throw new IllegalArgumentException("Service name/description/id is missing");
            }

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setServiceId(this.serviceId);
            service.setDescription(this.description);
            service.setName(this.name);
            service.setEvaluationOrder(this.evaluationOrder);
            service.setAccessStrategy(
                    new DefaultRegisteredServiceAccessStrategy(this.enabled, this.ssoEnabled));
            return service;
        }

    }
}
