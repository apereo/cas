package org.jasig.cas.support.rest;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * {@link RestController} implementation of a REST API
 * that allows for registration of CAS services.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RestController("registeredServiceResourceRestController")
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
     * @param tgtId ticket granting ticket id URI path param
     * @param serviceDataHolder the service to register and save in rest form
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/services/add/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createService(@ModelAttribute final ServiceDataHolder serviceDataHolder,
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


    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeValue(final String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
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
            if (StringUtils.isBlank(serviceId) || StringUtils.isBlank(name)
                    || StringUtils.isBlank(description)) {
                throw new IllegalArgumentException("Service name/description/id is missing");
            }

            final RegexRegisteredService service = new RegexRegisteredService();
            service.setServiceId(serviceId);
            service.setDescription(description);
            service.setName(name);
            service.setEvaluationOrder(evaluationOrder);
            service.setAccessStrategy(
                    new DefaultRegisteredServiceAccessStrategy(enabled, ssoEnabled));
            return service;
        }

    }
}
