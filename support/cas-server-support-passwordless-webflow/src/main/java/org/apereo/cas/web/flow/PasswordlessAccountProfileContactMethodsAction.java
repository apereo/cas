package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordlessAccountProfileContactMethodsAction}.
 * Shows in the account profile the email address and phone number that receive passwordless sign-in tokens,
 * as recorded by the passwordless account store. The store is read-only, so they cannot be changed here.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class PasswordlessAccountProfileContactMethodsAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute for the contact methods, each a map with {@code type} (email or phone) and {@code value}.
     */
    public static final String FLOW_SCOPE_ATTR_CONTACT_METHODS = "passwordlessContactMethods";

    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val username = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        try {
            val request = PasswordlessAuthenticationRequest.builder().username(username).providedUsername(username).build();
            passwordlessUserAccountStore.findUser(request).ifPresent(account -> {
                val methods = new ArrayList<Map<String, String>>();
                if (StringUtils.isNotBlank(account.getEmail())) {
                    methods.add(Map.of("type", "email", "value", account.getEmail()));
                }
                if (StringUtils.isNotBlank(account.getPhone())) {
                    methods.add(Map.of("type", "phone", "value", account.getPhone()));
                }
                requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_CONTACT_METHODS, methods);
            });
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return null;
    }
}
