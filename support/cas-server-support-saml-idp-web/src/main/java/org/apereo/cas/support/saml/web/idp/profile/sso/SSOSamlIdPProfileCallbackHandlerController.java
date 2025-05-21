package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.pac4j.jee.context.JEEContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerController}, which handles
 * the profile callback request to build the final saml response.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Tag(name = "SAML2")
public class SSOSamlIdPProfileCallbackHandlerController extends AbstractSamlIdPProfileHandlerController {

    public SSOSamlIdPProfileCallbackHandlerController(final SamlProfileHandlerConfigurationContext config) {
        super(config);
    }


    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
    @Operation(summary = "Handle SAML2 SSO Callback Profile Request")
    protected ModelAndView handleCallbackProfileRequestGet(final HttpServletResponse response,
                                                           final HttpServletRequest request) throws Throwable {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type.isBrowserStorage()
            && !request.getParameterMap().containsKey(BrowserStorage.PARAMETER_BROWSER_STORAGE)) {
            val context = new JEEContext(request, response);
            val sessionStorage = configurationContext.getSessionStore()
                .getTrackableSession(context).map(BrowserStorage.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to determine trackable session for storage"));
            return new ModelAndView(CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_READ,
                BrowserStorage.PARAMETER_BROWSER_STORAGE, sessionStorage);
        }
        return handleProfileRequest(response, request);
    }

    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
    @Operation(summary = "Handle SAML2 SSO Callback Profile Request")
    protected ModelAndView handleCallbackProfileRequestPost(final HttpServletResponse response,
                                                            final HttpServletRequest request) throws Throwable {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type.isBrowserStorage()) {
            val storage = WebUtils.getBrowserStoragePayload(request);
            if (storage.isPresent()) {
                val context = new JEEContext(request, response);
                configurationContext.getSessionStore().buildFromTrackableSession(context, storage.get());
                return handleProfileRequest(response, request);
            }
        }
        return WebUtils.produceErrorView(new IllegalArgumentException("Unable to build SAML response"));
    }

    private ModelAndView handleProfileRequest(final HttpServletResponse response, final HttpServletRequest request) throws Throwable {
        val authnContext = retrieveAuthenticationRequest(response, request);

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket) && authnContext.getKey() instanceof final AuthnRequest authnRequest
            && Boolean.FALSE.equals(authnRequest.isPassive())) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to handle SAML request"));
        }

        val authenticationContext = buildAuthenticationContextPair(request, response, authnContext);
        val assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        val binding = determineProfileBinding(authenticationContext, request);
        if (StringUtils.isBlank(binding)) {
            LOGGER.error("Unable to determine profile binding");
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to determine profile binding"));
        }
        val resultObject = buildSamlResponse(response, request, authenticationContext, assertion, binding, ticket);
        request.setAttribute(Response.class.getName(), resultObject);
        return null;
    }

    protected Optional<AuthenticatedAssertionContext> validateRequestAndBuildCasAssertion(
        final HttpServletResponse response,
        final HttpServletRequest request,
        final Pair<? extends RequestAbstractType, MessageContext> authnContext)
        throws Throwable {

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket) && authnContext.getKey() instanceof final AuthnRequest authnRequest
            && Boolean.TRUE.equals(authnRequest.isPassive())) {
            LOGGER.info("Unable to establish authentication context for passive authentication request");
            return Optional.empty();
        }

        val validator = getConfigurationContext().getTicketValidator();
        val serviceUrl = constructServiceUrl(request, response, authnContext);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        val assertion = validator.validate(ticket, serviceUrl);
        logCasValidationAssertion(assertion);

        val asserted = assertion.getAssertion();
        Objects.requireNonNull(asserted, "Validation assertion cannot be null");
        return Optional.of(AuthenticatedAssertionContext.builder()
            .name(assertion.getPrincipal().getId())
            .authenticationDate(DateTimeUtils.zonedDateTimeOf(asserted.getPrimaryAuthentication().getAuthenticationDate()))
            .attributes(CollectionUtils.merge(assertion.getAttributes(), assertion.getPrincipal().getAttributes()))
            .build());
    }
}
