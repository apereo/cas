package org.apereo.cas.support.saml.web.idp.profile.ecp;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link ECPSamlIdPProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class ECPSamlIdPProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public ECPSamlIdPProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    private static Credential extractBasicAuthenticationCredential(final HttpServletRequest request,
                                                                   final HttpServletResponse response) {
        try {
            val extractor = new BasicAuthExtractor();
            val webContext = new JEEContext(request, response, new JEESessionStore());
            val credentialsResult = extractor.extract(webContext);
            if (credentialsResult.isPresent()) {
                val credentials = credentialsResult.get();
                LOGGER.debug("Received basic authentication ECP request from credentials [{}]", credentials);
                return new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Handle ecp request.
     *
     * @param response the response
     * @param request  the request
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO,
        consumes = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE},
        produces = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE})
    public void handleEcpRequest(final HttpServletResponse response,
                                 final HttpServletRequest request) {
        val soapContext = decodeSoapRequest(request);
        val credential = extractBasicAuthenticationCredential(request, response);

        if (credential == null) {
            LOGGER.error("Credentials could not be extracted from the SAML ECP request");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (soapContext == null) {
            LOGGER.error("SAML ECP request could not be determined from the authentication request");
            return;
        }
        handleEcpRequest(response, request, soapContext, credential, SAMLConstants.SAML2_PAOS_BINDING_URI);
    }

    /**
     * Handle ecp request.
     *
     * @param response    the response
     * @param request     the request
     * @param soapContext the soap context
     * @param credential  the credential
     * @param binding     the binding
     */
    protected void handleEcpRequest(final HttpServletResponse response, final HttpServletRequest request,
                                    final MessageContext soapContext, final Credential credential,
                                    final String binding) {
        LOGGER.debug("Handling ECP request for SOAP context [{}]", soapContext);

        val envelope = soapContext.getSubcontext(SOAP11Context.class).getEnvelope();
        SamlUtils.logSamlObject(getSamlProfileHandlerConfigurationContext().getOpenSamlConfigBean(), envelope);

        val authnRequest = (AuthnRequest) soapContext.getMessage();
        val authenticationContext = Pair.of(authnRequest, soapContext);
        try {
            LOGGER.trace("Verifying ECP authentication request [{}]", authnRequest);
            val serviceRequest = verifySamlAuthenticationRequest(authenticationContext, request);

            LOGGER.trace("Attempting to authenticate ECP request for credential id [{}]", credential.getId());
            val authentication = authenticateEcpRequest(credential, authenticationContext);
            LOGGER.debug("Authenticated [{}] successfully with authenticated principal [{}]",
                credential.getId(), authentication.getPrincipal());

            LOGGER.trace("Building ECP SAML response for [{}]", credential.getId());
            val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            val service = getSamlProfileHandlerConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
            val casAssertion = buildCasAssertion(authentication, service, serviceRequest.getKey(), new LinkedHashMap<>(0));

            LOGGER.trace("CAS assertion to use for building ECP SAML response is [{}]", casAssertion);
            buildSamlResponse(response, request, authenticationContext, casAssertion, binding);
        } catch (final AuthenticationException e) {
            LOGGER.error(e.getMessage(), e);
            val error = e.getHandlerErrors().values()
                .stream()
                .map(Throwable::getMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
            buildEcpFaultResponse(response, request, Pair.of(authnRequest, error), soapContext);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            buildEcpFaultResponse(response, request, Pair.of(authnRequest, e.getMessage()), soapContext);
        }
    }

    /**
     * Build ecp fault response.
     *
     * @param response              the response
     * @param request               the request
     * @param authenticationContext the authentication context
     * @param messageContext        the message context
     */
    protected void buildEcpFaultResponse(final HttpServletResponse response,
                                         final HttpServletRequest request,
                                         final Pair<RequestAbstractType, String> authenticationContext,
                                         final MessageContext messageContext) {
        request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, authenticationContext.getValue());
        getSamlProfileHandlerConfigurationContext().getSamlFaultResponseBuilder()
            .build(authenticationContext.getKey(), request, response,
                null, null, null, SAMLConstants.SAML2_PAOS_BINDING_URI, messageContext);
    }

    /**
     * Authenticate ecp request.
     *
     * @param credential   the credential
     * @param authnRequest the authn request
     * @return the authentication
     */
    protected Authentication authenticateEcpRequest(final Credential credential,
                                                    final Pair<AuthnRequest, MessageContext> authnRequest) {
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest.getKey());
        LOGGER.debug("Located issuer [{}] from request prior to authenticating [{}]", issuer, credential.getId());

        val service = getSamlProfileHandlerConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
        LOGGER.debug("Executing authentication request for service [{}] on behalf of credential id [{}]", service, credential.getId());
        val authenticationResult = getSamlProfileHandlerConfigurationContext()
            .getAuthenticationSystemSupport().handleAndFinalizeSingleAuthenticationTransaction(service, credential);
        return authenticationResult.getAuthentication();
    }
}
