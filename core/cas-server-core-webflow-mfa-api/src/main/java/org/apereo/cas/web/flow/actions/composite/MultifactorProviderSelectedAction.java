package org.apereo.cas.web.flow.actions.composite;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
        val contextPath = context.getExternalContext().getContextPath();
        val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";
        val cookie = casProperties.getAuthn().getMfa().getCore().getProviderSelection().getCookie();
        if (cookie.isEnabled() && cookie.isAutoConfigureCookiePath()) {
            val path = multifactorProviderCookieBuilder.getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for multifactor authentication selection cookie generator to: [{}]", cookiePath);
                multifactorProviderCookieBuilder.setCookiePath(cookiePath);
            }
        }
        return super.doPreExecute(context);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val selectedProvider = WebUtils.getRequestParameterOrAttribute(requestContext, PARAMETER_SELECTED_MFA_PROVIDER)
            .orElseGet(() -> requestContext.getFlashScope().get(PARAMETER_SELECTED_MFA_PROVIDER, MultifactorAuthenticationProvider.class).getId());
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
