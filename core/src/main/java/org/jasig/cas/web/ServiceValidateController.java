/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.validation.Assertion;
import org.jasig.cas.validation.ValidationSpecification;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
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
public class ServiceValidateController extends AbstractController implements
    InitializingBean {

    /** View if Service Ticket Validation Fails. */
    private static final String DEFAULT_SERVICE_FAILURE_VIEW_NAME = "casServiceFailureView";

    /** View if Service Ticket Validation Succeeds. */
    private static final String DEFAULT_SERVICE_SUCCESS_VIEW_NAME = "casServiceSuccessView";

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

    protected void afterPropertiesSetInternal() throws Exception {
        // nothing to do
    }

    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null");

        Assert
            .notNull(
                this.proxyHandler,
                "proxyHandler cannot be null.  You may be seeing this because previous versions of CAS automatically set this.  Due to the use of HttpClient, this is no longer possible.");

        if (this.validationSpecificationClass == null) {
            this.validationSpecificationClass = Cas20ProtocolValidationSpecification.class;
            logger
                .info("No authentication specification class set.  Defaulting to "
                    + this.validationSpecificationClass.getName());
        }

        if (this.successView == null) {
            this.successView = DEFAULT_SERVICE_SUCCESS_VIEW_NAME;
            logger.info("No successView specified.  Using default of "
                + this.successView);
        }

        if (this.failureView == null) {
            this.failureView = DEFAULT_SERVICE_FAILURE_VIEW_NAME;
            logger.info("No failureView specified.  Using default of "
                + this.failureView);
        }

        afterPropertiesSetInternal();
    }

    /**
     * Overrideable method to determine which credentials to use to grant a
     * proxy granting ticket. Default is to use the pgtUrl.
     * 
     * @param request the HttpServletRequest object.
     * @return the credentials or null if there was an error or no credentials
     * provided.
     */
    protected Credentials getServiceCredentialsFromRequest(
        final HttpServletRequest request) {
        final String pgtUrl = request.getParameter(WebConstants.PGTURL);
        if (StringUtils.hasText(pgtUrl)) {
            try {
                return new HttpBasedServiceCredentials(new URL(pgtUrl));
            } catch (final Exception e) {
                logger.error("Error constructing pgtUrl", e);
            }
        }

        return null;
    }

    protected void initBinder(final HttpServletRequest request,
        final ServletRequestDataBinder binder) {
        binder.setRequiredFields(new String[] {"renew"});
    }

    protected final ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String serviceTicketId = request
            .getParameter(WebConstants.TICKET);
        final String service = request.getParameter(WebConstants.SERVICE);
        final Map model = new HashMap();
        final ValidationSpecification validationSpecification = this
            .getCommandClass();
        final Assertion assertion;
        String proxyGrantingTicketId = null;
        Credentials serviceCredentials = null;

        if (!StringUtils.hasText(service)
            || !StringUtils.hasText(serviceTicketId)) {
            model.put(WebConstants.CODE, "INVALID_REQUEST");
            model.put(WebConstants.DESC, getMessageSourceAccessor().getMessage(
                "INVALID_REQUEST", "INVALID_REQUEST"));
            return new ModelAndView(this.failureView, model);
        }

        final ServletRequestDataBinder binder = new ServletRequestDataBinder(
            validationSpecification, "validationSpecification");
        initBinder(request, binder);
        binder.bind(request);
        try {
            serviceCredentials = getServiceCredentialsFromRequest(request);
            if (serviceCredentials != null) {
                try {
                    proxyGrantingTicketId = this.centralAuthenticationService
                        .delegateTicketGrantingTicket(serviceTicketId,
                            serviceCredentials);
                } catch (TicketException e) {
                    logger.error("TicketException generating ticket for: "
                        + serviceCredentials, e);
                }
            }

            assertion = this.centralAuthenticationService
                .validateServiceTicket(serviceTicketId, new SimpleService(
                    WebUtils.stripJsessionFromUrl(service)));
            if (!validationSpecification.isSatisfiedBy(assertion)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ServiceTicket [" + serviceTicketId
                        + "] does not satisfy validation specification.");
                }

                model.put(WebConstants.CODE, "INVALID_TICKET");
                model.put(WebConstants.DESC, getMessageSourceAccessor()
                    .getMessage("INVALID_TICKET_SPEC", "INVALID_TICKET_SPEC"));
                return new ModelAndView(this.failureView, model);
            }

            if (serviceCredentials != null && proxyGrantingTicketId != null) {
                final String proxyIou = this.proxyHandler.handle(
                    serviceCredentials, proxyGrantingTicketId);
                model.put(WebConstants.PGTIOU, proxyIou);
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
