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
package org.jasig.cas.web;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketValidationException;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.validation.ValidationSpecification;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.view.CasViewConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Process the /validate , /serviceValidate , and /proxyValidate URL requests.
 * <p>
 * Obtain the Service Ticket and Service information and present them to the CAS
 * validation services. Receive back an Assertion containing the user Principal
 * and (possibly) a chain of Proxy Principals. Store the Assertion in the Model
 * and chain to a View to generate the appropriate response (CAS 1, CAS 2 XML,
 * SAML, ...).
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class ServiceValidateController extends DelegateController {
    /** View if Service Ticket Validation Fails. */
    public static final String DEFAULT_SERVICE_FAILURE_VIEW_NAME = "cas2ServiceFailureView";

    /** View if Service Ticket Validation Succeeds. */
    public static final String DEFAULT_SERVICE_SUCCESS_VIEW_NAME = "cas2ServiceSuccessView";

    /** Implementation of Service Manager. */
    @NotNull
    private ServicesManager servicesManager;
    
    /** The CORE which we will delegate all requests to. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    /** The validation protocol we want to use. */
    @NotNull
    private Class<?> validationSpecificationClass = Cas20ProtocolValidationSpecification.class;

    /** The proxy handler we want to use with the controller. */
    @NotNull
    private ProxyHandler proxyHandler;

    /** The view to redirect to on a successful validation. */
    @NotNull
    private String successView = DEFAULT_SERVICE_SUCCESS_VIEW_NAME;

    /** The view to redirect to on a validation failure. */
    @NotNull
    private String failureView = DEFAULT_SERVICE_FAILURE_VIEW_NAME;

    /** Extracts parameters from Request object. */
    @NotNull
    private ArgumentExtractor argumentExtractor;

    /**
     * Overrideable method to determine which credentials to use to grant a
     * proxy granting ticket. Default is to use the pgtUrl.
     *
     * @param service the webapp service requesting proxy
     * @param request the HttpServletRequest object.
     * @return the credentials or null if there was an error or no credentials
     * provided.
     */
    protected Credential getServiceCredentialsFromRequest(final WebApplicationService service, final HttpServletRequest request) {
        final String pgtUrl = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL);
        if (StringUtils.hasText(pgtUrl)) {
            try {
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                verifyRegisteredServiceProperties(registeredService, service);
                return new HttpBasedServiceCredential(new URL(pgtUrl), registeredService);
            } catch (final Exception e) {
                logger.error("Error constructing pgtUrl", e);
            }
        }

        return null;
    }

    /**
     * Inits the binder with the required fields. <code>renew</code> is required.
     *
     * @param request the request
     * @param binder the binder
     */
    protected void initBinder(final HttpServletRequest request, final ServletRequestDataBinder binder) {
        binder.setRequiredFields("renew");
    }

    @Override
    protected final ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final WebApplicationService service = this.argumentExtractor.extractService(request);
        final String serviceTicketId = service != null ? service.getArtifactId() : null;

        if (service == null || serviceTicketId == null) {
            logger.debug("Could not identify service and/or service ticket for service: [{}]", service);
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                    CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, null);
        }

        try {
            final Credential serviceCredential = getServiceCredentialsFromRequest(service, request);
            TicketGrantingTicket proxyGrantingTicketId = null;
            
            if (serviceCredential != null) {
                try {
                    proxyGrantingTicketId = this.centralAuthenticationService.delegateTicketGrantingTicket(serviceTicketId,
                                serviceCredential);
                    logger.debug("Generated PGT [{}] off of service ticket [{}] and credential [{}]",
                            proxyGrantingTicketId.getId(), serviceTicketId, serviceCredential);
                } catch (final AuthenticationException e) {
                    logger.info("Failed to authenticate service credential {}", serviceCredential);
                } catch (final TicketException e) {
                    logger.error("Failed to create proxy granting ticket for {}", serviceCredential, e);
                }
                
                if (proxyGrantingTicketId == null) {
                    return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                            CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                            new Object[] {serviceCredential.getId()});
                }
            }

            final Assertion assertion = this.centralAuthenticationService.validateServiceTicket(serviceTicketId, service);

            final ValidationSpecification validationSpecification = this.getCommandClass();
            final ServletRequestDataBinder binder = new ServletRequestDataBinder(validationSpecification, "validationSpecification");
            initBinder(request, binder);
            binder.bind(request);

            if (!validationSpecification.isSatisfiedBy(assertion)) {
                logger.debug("Service ticket [{}] does not satisfy validation specification.", serviceTicketId);
                return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_TICKET,
                        CasProtocolConstants.ERROR_CODE_INVALID_TICKET, null);
            }

            String proxyIou = null;
            if (serviceCredential != null && this.proxyHandler.canHandle(serviceCredential)) {
                proxyIou = this.proxyHandler.handle(serviceCredential, proxyGrantingTicketId);
                if (StringUtils.isEmpty(proxyIou)) {
                    return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                            CasProtocolConstants.ERROR_CODE_INVALID_PROXY_CALLBACK,
                            new Object[] {serviceCredential.getId()});
                }
            }

            onSuccessfulValidation(serviceTicketId, assertion);
            logger.debug("Successfully validated service ticket {} for service [{}]", serviceTicketId, service.getId());
            return generateSuccessView(assertion, proxyIou, service, proxyGrantingTicketId);
        } catch (final TicketValidationException e) {
            final String code = e.getCode();
            return generateErrorView(code, code,
                    new Object[] {serviceTicketId, e.getOriginalService().getId(), service.getId()});
        } catch (final TicketException te) {
            return generateErrorView(te.getCode(), te.getCode(),
                new Object[] {serviceTicketId});
        } catch (final UnauthorizedProxyingException e) {
            return generateErrorView(e.getMessage(), e.getMessage(), new Object[] {service.getId()});
        } catch (final UnauthorizedServiceException e) {
            return generateErrorView(e.getMessage(), e.getMessage(), null);
        }
    }

    /**
     * Triggered on successful validation events. Extensions are to
     * use this as hook to plug in behvior.
     *
     * @param serviceTicketId the service ticket id
     * @param assertion the assertion
     */
    protected void onSuccessfulValidation(final String serviceTicketId, final Assertion assertion) {
        // template method with nothing to do.
    }

    /**
     * Generate error view, set to {@link #setFailureView(String)}.
     *
     * @param code the code
     * @param description the description
     * @param args the args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final String description, final Object[] args) {
        final ModelAndView modelAndView = new ModelAndView(this.failureView);
        final String convertedDescription = getMessageSourceAccessor().getMessage(description, args, description);
        modelAndView.addObject("code", code);
        modelAndView.addObject("description", convertedDescription);

        return modelAndView;
    }

    /**
     * Generate the success view. The result will contain the assertion and the proxy iou.
     *
     * @param assertion the assertion
     * @param proxyIou the proxy iou
     * @param service the validated service
     * @param proxyGrantingTicket the proxy granting ticket
     * @return the model and view, pointed to the view name set by
     */
    private ModelAndView generateSuccessView(final Assertion assertion, final String proxyIou,
                                             final WebApplicationService service,
                                             final TicketGrantingTicket proxyGrantingTicket) {

        final ModelAndView success = new ModelAndView(this.successView);
        success.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_ASSERTION, assertion);
        success.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_SERVICE, service);
        success.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU, proxyIou);
        if (proxyGrantingTicket != null) {
            success.addObject(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, proxyGrantingTicket.getId());
        }
        final Map<String, ?> augmentedModelObjects = augmentSuccessViewModelObjects(assertion);
        if (augmentedModelObjects != null) {
            success.addAllObjects(augmentedModelObjects);
        }
        return success;
    }

    /**
     * Augment success view model objects. Provides
     * a way for extension of this controller to dynamically
     * populate the model object with attributes
     * that describe a custom nature of the validation protocol.
     *
     * @param assertion the assertion
     * @return map of objects each keyed to a name
     */
    protected Map<String, ?> augmentSuccessViewModelObjects(final Assertion assertion) {
        return Collections.emptyMap();  
    }
    
    /**
     * Gets the command class based on {@link #setValidationSpecificationClass(Class)}.
     *
     * @return the command class
     */
    private ValidationSpecification getCommandClass() {
        try {
            return (ValidationSpecification) this.validationSpecificationClass.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        return true;
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public final void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    
    public final void setArgumentExtractor(final ArgumentExtractor argumentExtractor) {
        this.argumentExtractor = argumentExtractor;
    }

    /**
     * @param validationSpecificationClass The authenticationSpecificationClass
     * to set.
     */
    public final void setValidationSpecificationClass(final Class<?> validationSpecificationClass) {
        this.validationSpecificationClass = validationSpecificationClass;
    }

    /**
     * @param failureView The failureView to set.
     */
    public final void setFailureView(final String failureView) {
        this.failureView = failureView;
    }

    /**
     * @param successView The successView to set.
     */
    public final void setSuccessView(final String successView) {
        this.successView = successView;
    }

    /**
     * @param proxyHandler The proxyHandler to set.
     */
    public final void setProxyHandler(final ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    /**
     * Sets the services manager.
     *
     * @param servicesManager the new services manager
     */
    public final void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Ensure that the service is found and enabled in the service registry.
     * @param registeredService the located entry in the registry
     * @param service authenticating service
     * @throws UnauthorizedServiceException
     */
    private void verifyRegisteredServiceProperties(final RegisteredService registeredService, final Service service) {
        if (registeredService == null) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not found in service registry.", service.getId());
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not enabled in service registry.", service.getId());
            
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }
}
