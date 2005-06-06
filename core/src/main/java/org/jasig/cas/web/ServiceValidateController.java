/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationSpecification;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Process the /validate and /serviceValidate URL requests.
 * <p>
 * Obtain the Service Ticket and Service information and present them to the CAS
 * validation services. Receive back an Assertion containing the user Principal
 * and (possibly) a chain of Proxy Principals. Store the Assertion in the Model
 * and chain to a View to generate the appropriate response (CAS 1, CAS 2 XML,
 * SAML, ...).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceValidateController extends AbstractController
    implements InitializingBean {

    /** Logger to log events and errors. */
    private final Log log = LogFactory.getLog(getClass());

    /** The CORE which we will delegate all requests to. */
    private CentralAuthenticationService centralAuthenticationService;

    /** The validation protocol we want to use. */
    private Class validationSpecificationClass;

    /** The proxy handler we want to use with the controller. */
    private ProxyHandler proxyHandler;

    /** The view to redirect to on a successful validation. */
    private String successView;

    /** The view to redirect to on a validation failure. */
    private String failureView;

    public void afterPropertiesSet() throws Exception {
        final String name = this.getClass().getName();

        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null on " + name);

        if (this.validationSpecificationClass == null) {
            this.validationSpecificationClass = Cas20ProtocolValidationSpecification.class;
            log
                .info("No authentication specification class set.  Defaulting to "
                    + this.validationSpecificationClass.getName());
        }

        if (this.successView == null) {
            this.successView = ViewNames.CONST_SERVICE_SUCCESS;
            log.info("No successView specified.  Using default of "
                + this.successView);
        }

        if (this.failureView == null) {
            this.failureView = ViewNames.CONST_SERVICE_FAILURE;
            log.info("No failureView specified.  Using default of "
                + this.failureView);
        }

        if (this.proxyHandler == null) {
            this.proxyHandler = new Cas20ProxyHandler();
            log.info("No proxyHandler specified.  Defaulting to "
                + this.proxyHandler.getClass().getName());
        }
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String serviceTicketId = request
            .getParameter(WebConstants.TICKET);
        final String service = request.getParameter(WebConstants.SERVICE);
        final Map model = new HashMap();
        final ValidationSpecification authenticationSpecification = this
            .getCommandClass();
        final Assertion assertion;
        final String pgtUrl = request.getParameter(WebConstants.PGTURL);

        if (!StringUtils.hasText(service)
            || !StringUtils.hasText(serviceTicketId)) {
            model.put(WebConstants.CODE, "INVALID_REQUEST");
            model.put(WebConstants.DESC, getMessageSourceAccessor().getMessage(
                "INVALID_REQUEST", "INVALID_REQUEST"));
            return new ModelAndView(this.failureView, model);
        }

        BindUtils.bind(request, authenticationSpecification,
            "authenticationSpecification");
        try {
            assertion = this.centralAuthenticationService
                .validateServiceTicket(serviceTicketId, new SimpleService(
                    service));
            if (!authenticationSpecification.isSatisfiedBy(assertion)) {
                log.debug("ServiceTicket [" + serviceTicketId
                    + "] does not satisfy authentication specification.");

                model.put(WebConstants.CODE, "INVALID_TICKET");
                model.put(WebConstants.DESC, getMessageSourceAccessor()
                    .getMessage("INVALID_TICKET_SPEC", "INVALID_TICKET_SPEC"));
                return new ModelAndView(this.failureView, model);
            }

            if (StringUtils.hasText(pgtUrl)) {
                try {
                    final Credentials serviceCredentials = new HttpBasedServiceCredentials(
                        new URL(pgtUrl));
                    final String proxyGrantingTicketId = this.centralAuthenticationService
                        .delegateTicketGrantingTicket(serviceTicketId,
                            serviceCredentials);

                    final String proxyIou = this.proxyHandler.handle(
                        serviceCredentials, proxyGrantingTicketId);
                    model.put(WebConstants.PGTIOU, proxyIou);
                } catch (MalformedURLException e) {
                    log
                        .debug("Error attempting to convert pgtUrl from String to URL.  pgtUrl was: "
                            + pgtUrl);
                } catch (TicketException e) {
                    log.error("TicketException generating ticket for: "
                        + pgtUrl);
                }
            }
            model.put(WebConstants.ASSERTION, assertion);

            return new ModelAndView(this.successView, model);
        } catch (TicketException te) {
            model.put(WebConstants.CODE, te.getCode());
            model.put(WebConstants.DESC, getMessageSourceAccessor().getMessage(
                te.getCode(), new Object[] {serviceTicketId}, te.getCode()));
            return new ModelAndView(this.failureView, model);
        }
    }

    private ValidationSpecification getCommandClass() {
        try {
            return (ValidationSpecification) this.validationSpecificationClass
                .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * @param validationSpecificationClass The authenticationSpecificationClass
     * to set.
     */
    public void setValidationSpecificationClass(
        final Class validationSpecificationClass) {
        this.validationSpecificationClass = validationSpecificationClass;
    }

    /**
     * @param failureView The failureView to set.
     */
    public void setFailureView(final String failureView) {
        this.failureView = failureView;
    }

    /**
     * @param successView The successView to set.
     */
    public void setSuccessView(final String successView) {
        this.successView = successView;
    }

    /**
     * @param proxyHandler The proxyHandler to set.
     */
    public void setProxyHandler(final ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }
}
