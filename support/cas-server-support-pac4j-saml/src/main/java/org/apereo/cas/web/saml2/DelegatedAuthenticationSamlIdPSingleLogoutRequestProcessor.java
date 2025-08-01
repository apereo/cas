package org.apereo.cas.web.saml2;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.slo.SamlIdPProfileSingleLogoutRequestProcessor;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedAuthenticationSamlIdPSingleLogoutRequestProcessor implements SamlIdPProfileSingleLogoutRequestProcessor {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    @Deprecated(since = "7.3.0", forRemoval = true)
    private final CasCookieBuilder delegatedSaml2IdPSloRequestCookieGenerator;
    private final ServiceFactory serviceFactory;
    private final ServicesManager servicesManager;
    private final CasConfigurationProperties casProperties;

    @Override
    public void receive(final HttpServletRequest request, final HttpServletResponse response,
                        final LogoutRequest logoutRequest, final MessageContext messageContext) throws Exception {
        autoConfigureCookieIfNecessary(request);
        createSamlLogoutRequestCookie(request, response, logoutRequest, messageContext);
    }

    @Override
    public void restore(final RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var registeredService = WebUtils.getRegisteredService(request);
        val cookieValue = delegatedSaml2IdPSloRequestCookieGenerator.retrieveCookieValue(request);

        if (registeredService == null && StringUtils.isNotBlank(cookieValue)) {
            val decodedCookieValue = EncodingUtils.decodeBase64(cookieValue);
            val cookieValues = MAPPER.readValue(decodedCookieValue, Map.class);
            val samlLogoutRequest = (String) cookieValues.get(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
            val entityId = (String) cookieValues.get(SamlProtocolConstants.PARAMETER_ENTITY_ID);
            val relayState = (String) cookieValues.get(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);

            val service = serviceFactory.createService(entityId);
            service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, entityId);
            service.getAttributes().put(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlLogoutRequest);
            registeredService = servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            WebUtils.putRegisteredService(request, registeredService);
            WebUtils.putSingleLogoutRequest(request, samlLogoutRequest);

            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, relayState);
            requestContext.getFlowScope().put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, relayState);
            delegatedSaml2IdPSloRequestCookieGenerator.removeCookie(response);
        }

    }

    protected void createSamlLogoutRequestCookie(final HttpServletRequest request, final HttpServletResponse response,
                                                 final LogoutRequest logoutRequest, final MessageContext messageContext) throws Exception {
        val relayState = StringUtils.defaultString(SAMLBindingSupport.getRelayState(messageContext));
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(logoutRequest);
        val sloRequest = StringUtils.defaultString(WebUtils.getSingleLogoutRequest(request));
        val payload = new HashMap<String, Object>();
        payload.put(SamlProtocolConstants.PARAMETER_ENTITY_ID, issuer);
        FunctionUtils.doIfNotBlank(relayState, value -> payload.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, value));
        FunctionUtils.doIfNotBlank(relayState, value -> payload.put(SamlProtocolConstants.PARAMETER_SAML_REQUEST, sloRequest));
        val cookieValue = EncodingUtils.encodeBase64(MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
        delegatedSaml2IdPSloRequestCookieGenerator.addCookie(request, response, cookieValue);
    }

    @Deprecated(since = "7.3.0", forRemoval = true)
    protected void autoConfigureCookieIfNecessary(final HttpServletRequest request) {
        val cookieProps = casProperties.getAuthn().getPac4j().getCore().getSessionReplication().getCookie();
        if (cookieProps.isAutoConfigureCookiePath()) {
            CookieUtils.configureCookiePath(request, delegatedSaml2IdPSloRequestCookieGenerator);
        }
    }
}
