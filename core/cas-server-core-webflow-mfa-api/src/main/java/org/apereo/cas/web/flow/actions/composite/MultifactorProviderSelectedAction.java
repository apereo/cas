package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorProviderSelectedAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorProviderSelectedAction extends BaseCasWebflowAction {
    /**
     * The selected MFA provider.
     */
    static final String PARAMETER_SELECTED_MFA_PROVIDER = "mfaProvider";

    private final CasCookieBuilder multifactorProviderCookieBuilder;
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        val cookie = casProperties.getAuthn().getMfa().getCore().getProviderSelection().getCookie();
        if (cookie.isEnabled() && cookie.isAutoConfigureCookiePath()) {
            CookieUtils.configureCookiePath(context, multifactorProviderCookieBuilder);
        }
        return super.doPreExecute(context);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val mfaOptional = casProperties.getAuthn().getMfa().getCore().getProviderSelection().isProviderSelectionOptional();
        val selectedProvider = WebUtils.getRequestParameterOrAttribute(requestContext, PARAMETER_SELECTED_MFA_PROVIDER)
            .orElseGet(() -> requestContext.getFlashScope().get(PARAMETER_SELECTED_MFA_PROVIDER, MultifactorAuthenticationProvider.class).getId());
        if (mfaOptional && Strings.CI.equals(selectedProvider, "none")) {
            LOGGER.debug("No multifactor authentication provider is selected, and provider selection is optional. Proceeding with authentication without MFA");
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        LOGGER.debug("Selected multifactor authentication provider is [{}]", selectedProvider);
        rememberSelectedMultifactorAuthenticationProvider(requestContext, selectedProvider);
        return eventFactory.event(this, selectedProvider);
    }

    protected void rememberSelectedMultifactorAuthenticationProvider(final RequestContext requestContext, final String selectedProvider) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        multifactorProviderCookieBuilder.addCookie(request, response, selectedProvider);
    }
}
