package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnStartRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class WebAuthnStartRegistrationAction extends AbstractMultifactorAuthenticationAction<WebAuthnMultifactorAuthenticationProvider> {

    /**
     * Attribute name that points to the web application id put into the webflow.
     */
    public static final String FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID = "webauthnApplicationId";

    protected final CasConfigurationProperties casProperties;

    protected final TenantExtractor tenantExtractor;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
        val authn = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authn.getPrincipal(), requestContext);
        val attributes = principal.getAttributes();

        LOGGER.debug("Starting registration sequence for [{}]", principal);
        val flowScope = requestContext.getFlowScope();
        if (attributes.containsKey(webAuthn.getDisplayNameAttribute())) {
            CollectionUtils.firstElement(attributes.get(webAuthn.getDisplayNameAttribute()))
                .ifPresent(value -> flowScope.put("displayName", value));
        } else {
            flowScope.put("displayName", principal.getId());
        }
        flowScope.put(FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID, webAuthn.getApplicationId());
        return null;
    }
}
