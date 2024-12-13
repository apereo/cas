package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PreparePasswordlessSelectionMenuAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class PreparePasswordlessSelectionMenuAction extends BasePasswordlessCasWebflowAction {
    protected final PasswordlessUserAccountStore passwordlessUserAccountStore;

    protected final ConfigurableApplicationContext applicationContext;

    public PreparePasswordlessSelectionMenuAction(final CasConfigurationProperties casProperties,
                                                  final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                  final PrincipalFactory passwordlessPrincipalFactory,
                                                  final AuthenticationSystemSupport authenticationSystemSupport,
                                                  final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                  final ConfigurableApplicationContext applicationContext) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        FunctionUtils.throwIf(!user.isAllowSelectionMenu(), () -> new IllegalStateException("Passwordless account is not allowed to select options"));
        PasswordlessWebflowUtils.putMultifactorAuthenticationAllowed(requestContext, shouldActivateMultifactorAuthenticationFor(requestContext, user));
        PasswordlessWebflowUtils.putDelegatedAuthenticationAllowed(requestContext, isDelegatedAuthenticationActiveFor(requestContext, user));
        return null;
    }
}
