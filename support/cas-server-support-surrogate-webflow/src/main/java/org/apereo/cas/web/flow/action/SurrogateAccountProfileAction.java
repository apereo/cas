package org.apereo.cas.web.flow.action;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateAccountProfileAction}.
 * Shows in the account profile the accounts the user may impersonate, and,
 * when the current session is an impersonation, the user who started it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SurrogateAccountProfileAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute for the sorted accounts the user may impersonate.
     */
    public static final String FLOW_SCOPE_ATTR_ACCOUNTS = "surrogateAccounts";

    /**
     * Flow scope attribute set when the user may impersonate any account.
     */
    public static final String FLOW_SCOPE_ATTR_WILDCARD = "surrogateWildcard";

    /**
     * Flow scope attribute for the user who started the current impersonation session.
     */
    public static final String FLOW_SCOPE_ATTR_IMPERSONATED_BY = "surrogateImpersonatedBy";

    private final SurrogateAuthenticationService surrogateAuthenticationService;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val attributes = authentication.getAttributes();
        val impersonatedBy = CollectionUtils.firstElement(attributes.get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL))
            .map(Object::toString);
        impersonatedBy.ifPresent(primary -> requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_IMPERSONATED_BY, primary));
        val username = impersonatedBy.orElseGet(() -> authentication.getPrincipal().getId());
        try {
            val service = Optional.<Service>empty();
            val accounts = surrogateAuthenticationService.getImpersonationAccounts(username, service);
            if (surrogateAuthenticationService.isWildcardedAccount(accounts, service)) {
                requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_WILDCARD, Boolean.TRUE);
            } else if (!accounts.isEmpty()) {
                requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_ACCOUNTS, accounts.stream().sorted().toList());
            }
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }
}
