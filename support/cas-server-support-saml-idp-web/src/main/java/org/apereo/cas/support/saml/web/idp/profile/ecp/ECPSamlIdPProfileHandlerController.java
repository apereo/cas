package org.apereo.cas.support.saml.web.idp.profile.ecp;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link ECPSamlIdPProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Tag(name = "SAML2")
public class ECPSamlIdPProfileHandlerController extends AbstractSamlIdPProfileHandlerController {
    public ECPSamlIdPProfileHandlerController(final SamlProfileHandlerConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle ecp request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO,
        consumes = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE},
        produces = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE})
    @Operation(summary = "Handle SAML ECP request")
    public void handleEcpRequest(final HttpServletResponse response,
                                 final HttpServletRequest request) throws Exception {
        val soapContext = decodeSoapRequest(request);
        val credential = extractBasicAuthenticationCredential(request);

        if (credential == null) {
            LOGGER.error("Credentials could not be extracted from the SAML ECP request");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (soapContext == null) {
            LOGGER.error("SAML ECP request could not be determined from the authentication request");
            return;
        }
        val buildContext = SamlProfileBuilderContext.builder()
            .httpRequest(request)
            .httpResponse(response)
            .binding(SAMLConstants.SAML2_PAOS_BINDING_URI)
            .messageContext(soapContext)
            .build();
        handleEcpRequest(buildContext, credential);
    }

    protected void handleEcpRequest(final SamlProfileBuilderContext context, final Credential credential) throws Exception {
        LOGGER.debug("Handling ECP request for SOAP context [{}]", context.getMessageContext());

        val envelope = context.getMessageContext().getSubcontext(SOAP11Context.class).getEnvelope();
        getConfigurationContext().getOpenSamlConfigBean().logObject(envelope);

        val authnRequest = (AuthnRequest) context.getMessageContext().getMessage();
        val authenticationContext = Pair.of(authnRequest, context.getMessageContext());
        try {
            LOGGER.trace("Verifying ECP authentication request [{}]", authnRequest);
            val serviceRequest = verifySamlAuthenticationRequest(authenticationContext, context.getHttpRequest());

            LOGGER.trace("Attempting to authenticate ECP request for credential id [{}]", credential.getId());
            val authentication = authenticateEcpRequest(credential, authenticationContext);
            LOGGER.debug("Authenticated [{}] successfully with authenticated principal [{}]",
                credential.getId(), authentication.getPrincipal());

            LOGGER.trace("Building ECP SAML response for [{}]", credential.getId());
            val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
            val service = getConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
            val casAssertion = buildCasAssertion(authentication, service, serviceRequest.getKey(), new LinkedHashMap<>());

            LOGGER.trace("CAS assertion to use for building ECP SAML2 response is [{}]", casAssertion);
            buildSamlResponse(context.getHttpResponse(), context.getHttpRequest(),
                authenticationContext, Optional.of(casAssertion), context.getBinding(), null);
        } catch (final AuthenticationException e) {
            LoggingUtils.error(LOGGER, e);
            val error = e.getHandlerErrors().values()
                .stream()
                .map(Throwable::getMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
            buildEcpFaultResponse(context, error);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            buildEcpFaultResponse(context, e.getMessage());
        }
    }

    protected void buildEcpFaultResponse(final SamlProfileBuilderContext context, final String error) throws Exception {
        context.getHttpRequest().setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, error);
        getConfigurationContext().getSamlFaultResponseBuilder().build(context);
    }

    protected Authentication authenticateEcpRequest(final Credential credential,
                                                    final Pair<AuthnRequest, MessageContext> authnRequest) throws Throwable {
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest.getKey());
        LOGGER.debug("Located issuer [{}] from request prior to authenticating [{}]", issuer, credential.getId());

        val service = getConfigurationContext().getWebApplicationServiceFactory().createService(issuer);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(issuer));
        LOGGER.debug("Executing authentication request for service [{}] on behalf of credential id [{}]", service, credential.getId());
        val authenticationResult = getConfigurationContext()
            .getAuthenticationSystemSupport().finalizeAuthenticationTransaction(service, credential);
        return authenticationResult.getAuthentication();
    }

    private static Credential extractBasicAuthenticationCredential(final HttpServletRequest request) {
        val converter = new BasicAuthenticationConverter();
        val token = converter.convert(request);
        return FunctionUtils.doIfNotNull(token, () -> {
            LOGGER.debug("Received basic authentication ECP request from credentials [{}]", token.getPrincipal());
            return new UsernamePasswordCredential(token.getPrincipal().toString(), token.getCredentials().toString());
        });
    }
}
