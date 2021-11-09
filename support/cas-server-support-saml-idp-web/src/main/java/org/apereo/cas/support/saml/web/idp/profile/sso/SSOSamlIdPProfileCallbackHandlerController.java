package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPCoreProperties;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerController}, which handles
 * the profile callback request to build the final saml response.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
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
    protected ModelAndView handleCallbackProfileRequestGet(final HttpServletResponse response,
                                                           final HttpServletRequest request) throws Exception {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE
            && !request.getParameterMap().containsKey(BrowserSessionStorage.KEY_SESSION_STORAGE)) {
            return new ModelAndView(CasWebflowConstants.VIEW_ID_SESSION_STORAGE_READ);
        }
        return handleProfileRequest(response, request);
    }

    /**
     * Handle callback profile request post.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
    protected ModelAndView handleCallbackProfileRequestPost(final HttpServletResponse response,
                                                            final HttpServletRequest request) throws Exception {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE) {
            val storage = request.getParameter(BrowserSessionStorage.KEY_SESSION_STORAGE);
            val context = new JEEContext(request, response);
            configurationContext.getSessionStore().buildFromTrackableSession(context, storage);
            return handleProfileRequest(response, request);
        }
        return WebUtils.produceErrorView(new IllegalArgumentException("Unable to build SAML response"));
    }


    private ModelAndView handleProfileRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        val authnContext = retrieveAuthenticationRequest(response, request);

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to handle SAML request"));
        }

        val authenticationContext = buildAuthenticationContextPair(request, response, authnContext);
        val assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        val binding = determineProfileBinding(authenticationContext);
        if (StringUtils.isBlank(binding)) {
            LOGGER.error("Unable to determine profile binding");
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to determine profile binding"));
        }
        buildSamlResponse(response, request, authenticationContext, assertion, binding);
        return null;
    }

    private AuthenticatedAssertionContext validateRequestAndBuildCasAssertion(
        final HttpServletResponse response,
        final HttpServletRequest request,
        final Pair<? extends RequestAbstractType, MessageContext> authnContext)
        throws Exception {

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        val validator = getConfigurationContext().getTicketValidator();
        val serviceUrl = constructServiceUrl(request, response, authnContext);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        val assertion = validator.validate(ticket, serviceUrl);
        logCasValidationAssertion(assertion);
        return AuthenticatedAssertionContext.builder()
            .name(assertion.getPrincipal().getName())
            .authenticationDate(DateTimeUtils.zonedDateTimeOf(assertion.getAuthenticationDate()))
            .validFromDate(DateTimeUtils.zonedDateTimeOf(assertion.getValidFromDate()))
            .validUntilDate(DateTimeUtils.zonedDateTimeOf(assertion.getValidUntilDate()))
            .attributes(CollectionUtils.merge(assertion.getAttributes(), assertion.getPrincipal().getAttributes()))
            .build();
    }
}
