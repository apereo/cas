package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class YubiKeyAccountSaveRegistrationAction extends AbstractMultifactorAuthenticationAction<YubiKeyMultifactorAuthenticationProvider> {

    /**
     * Token parameter.
     */
    public static final String PARAMETER_NAME_TOKEN = "token";

    /**
     * Account name parameter.
     */
    public static final String PARAMETER_NAME_ACCOUNT = "accountName";

    private static final String CODE_FAILURE = "cas.mfa.yubikey.register.fail";

    protected final YubiKeyAccountRegistry registry;
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val authentication = WebUtils.getAuthentication(requestContext);
            val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
            val uid = principal.getId();
            val token = WebUtils.getRequestParameterOrAttribute(requestContext, PARAMETER_NAME_TOKEN).orElseThrow();
            val accountName = WebUtils.getRequestParameterOrAttribute(requestContext, PARAMETER_NAME_ACCOUNT).orElseThrow();

            val regRequest = YubiKeyDeviceRegistrationRequest.builder()
                .username(uid)
                .name(accountName)
                .token(token)
                .tenant(tenantExtractor.extract(requestContext).map(TenantDefinition::getId).orElse(null))
                .build();

            if (StringUtils.isNotBlank(token) && registry.registerAccountFor(regRequest)) {
                return success();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, CODE_FAILURE);
        return error();
    }
}
