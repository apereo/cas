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

package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.SamlIdPConstants;
import org.jasig.cas.support.saml.web.idp.SamlResponseBuilder;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;

/**
 * The {@link SSOPostProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller("ssoPostProfileHandlerController")
public class SSOPostProfileHandlerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationRedirectStrategy authenticationRedirectStrategy
            = new DefaultAuthenticationRedirectStrategy();

    @Autowired
    private ParserPool parserPool;

    @NotNull
    @Value("${server.name}")
    private String casServerName;

    @NotNull
    @Value("${server.prefix}")
    private String casServerPrefix;

    @NotNull
    @Value("${server.prefix}/login")
    private String casServerLoginUrl;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private Service callbackService;

    @Autowired
    @Qualifier("ssoPostProfileSamlResponseBuilder")
    private SamlResponseBuilder responseBuilder;

    /**
     * Instantiates a new redirect profile handler controller.
     */
    public SSOPostProfileHandlerController() {

    }

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    public void initialize() {
        this.callbackService = webApplicationServiceFactory.createService(
                this.casServerPrefix.concat(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK));
        logger.debug("Initialized callback service url {}", this.callbackService);

        if (!servicesManager.matchesExistingService(this.callbackService))  {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(new SecureRandom().nextLong());
            service.setName(service.getClass().getSimpleName());
            service.setDescription(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK.concat(" Callback Url"));
            service.setServiceId(this.callbackService.getId());

            logger.debug("Saving callback service {} into the registry", service);
            this.servicesManager.save(service);
        }
    }

    private void storeAuthnRequest(final HttpServletRequest request, final AuthnRequest authnRequest) {
        final HttpSession session = request.getSession();
        session.setAttribute(AuthnRequest.class.getName(), authnRequest);
    }

    private AuthnRequest retrieveAuthnRequest(final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        return (AuthnRequest) session.getAttribute(AuthnRequest.class.getName());
    }

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path=SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK, method = RequestMethod.GET)
    protected void handleCallbackProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request) throws Exception {
        logger.info("Received SAML callback profile request {}", request.getRequestURI());
        final AuthnRequest authnRequest = retrieveAuthnRequest(request);
        if (authnRequest == null) {
            logger.error("Can not validate the request because the original Authn request can not be found.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            logger.error("Can not validate the request because no {} is provided via the request",
                    CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(this.casServerPrefix);
        validator.setRenew(authnRequest.isForceAuthn());
        final String serviceUrl = constructServiceUrl(request, response,
                this.callbackService.getId(), this.casServerName);
        logger.debug("Created service url for validation: {}", serviceUrl);
        final Assertion assertion = validator.validate(ticket, serviceUrl);
        logValidationAssertion(assertion);
        if (assertion.isValid()) {
            logger.debug("Preparing SAML response to {}", authnRequest.getAssertionConsumerServiceURL());
            final Response samlResponse = responseBuilder.build(authnRequest, request, response, assertion);
        }
        storeAuthnRequest(request, null);
    }



    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request the request
     * @throws Exception the exception
     */
    @RequestMapping(path= SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request) throws Exception {
        logger.info("Received SAML profile request {}", request.getRequestURI());

        final SAMLObject messageContext = decodeAuthnRequest(request);
        if (messageContext instanceof AuthnRequest) {
            final AuthnRequest authnRequest = (AuthnRequest) messageContext;
            storeAuthnRequest(request, authnRequest);
            logAuthnRequest(authnRequest);
            performAuthentication(authnRequest, request, response);
        }
    }

    private void logValidationAssertion(final Assertion assertion) {
        logger.debug("Assertion Valid: {}", assertion.isValid());
        logger.debug("Assertion Principal: {}", assertion.getPrincipal().getName());
        logger.debug("Assertion AuthN Date: {}", assertion.getAuthenticationDate());
        logger.debug("Assertion ValidFrom Date: {}", assertion.getValidFromDate());
        logger.debug("Assertion ValidUntil Date: {}", assertion.getValidUntilDate());
        logger.debug("Assertion Attributes: {}", assertion.getAttributes());
        logger.debug("Assertion Principal Attributes: {}", assertion.getPrincipal().getAttributes());
    }

    private void performAuthentication(final AuthnRequest authnRequest,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) throws Exception {
        final String serviceUrl = constructServiceUrl(request, response, this.callbackService.getId(),
                this.casServerName);
        logger.debug("Created service url {}", serviceUrl);

        final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl,
                CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
                authnRequest.isPassive());

        logger.debug("Redirecting SAML authN request to \"{}\"", urlToRedirectTo);
        this.authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);

    }

    private SAMLObject decodeAuthnRequest(final HttpServletRequest request) {
        try {
            final HTTPPostDecoder decoder = new HTTPPostDecoder();
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();
            return decoder.getMessageContext().getMessage();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void logAuthnRequest(final AuthnRequest authnRequest) {
        logger.debug("\t Request Issuer: {}", authnRequest.getIssuer().getValue());
        logger.debug("\t AssertionConsumerServiceURL: {}", authnRequest.getAssertionConsumerServiceURL());
        logger.debug("\t Destination: {}", authnRequest.getDestination());
        logger.debug("\t ProtocolBinding: {}", authnRequest.getProtocolBinding());
        logger.debug("\t Forced AuthN: {}", authnRequest.isForceAuthn());
        logger.debug("\t Passive AuthN: {}", authnRequest.isPassive());
        logger.debug("\t Signed AuthN: {}", authnRequest.isSigned());

        if (StringUtils.isNotBlank(authnRequest.getProviderName())) {
            logger.debug("\t ProviderName: {}", authnRequest.getProviderName());
        }
        logger.debug("\t IssueInstant: {}", authnRequest.getIssueInstant());

        if (authnRequest.getRequestedAuthnContext() != null) {
            for (final AuthnContextClassRef ctx : authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()) {
                logger.debug("\t AuthnContextClassRef: {}", ctx.getAuthnContextClassRef());
            }

            for (final AuthnContextDeclRef ctx : authnRequest.getRequestedAuthnContext().getAuthnContextDeclRefs()) {
                logger.debug("\t AuthnContextClassRef: {}", ctx.getAuthnContextDeclRef());
            }
            logger.debug("\t AuthnContextClass Comparison: {}",
                    authnRequest.getRequestedAuthnContext().getComparison());
        }

        if (authnRequest.getNameIDPolicy() != null) {
            logger.debug("\t NameIDFormat: {}", authnRequest.getNameIDPolicy().getFormat());
            logger.debug("\t SPNameQualifier: {}", authnRequest.getNameIDPolicy().getSPNameQualifier());
        }
    }


    private String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                       final String service, final String serverName) {
        return CommonUtils.constructServiceUrl(request, response,
                service, serverName,
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
    }

}
