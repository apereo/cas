package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link PreparePasswordlessSelectionMenuAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class PreparePasswordlessSelectionMenuAction extends BasePasswordlessCasWebflowAction {
    protected final PasswordlessUserAccountStore passwordlessUserAccountStore;

    private final ObjectProvider<@NonNull DelegatedClientIdentityProviderConfigurationProducer> providerConfigurationProducer;

    private final CommunicationsManager communicationsManager;

    public PreparePasswordlessSelectionMenuAction(
        final CasConfigurationProperties casProperties,
        final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
        final PrincipalFactory passwordlessPrincipalFactory,
        final AuthenticationSystemSupport authenticationSystemSupport,
        final PasswordlessUserAccountStore passwordlessUserAccountStore,
        final ObjectProvider<@NonNull DelegatedClientIdentityProviderConfigurationProducer> providerConfigurationProducer,
        final CommunicationsManager communicationsManager) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.providerConfigurationProducer = providerConfigurationProducer;
        this.communicationsManager = communicationsManager;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        FunctionUtils.throwIf(!user.isAllowSelectionMenu(), () -> new IllegalStateException("Passwordless account is not allowed to select options"));
        PasswordlessWebflowUtils.putMultifactorAuthenticationAllowed(requestContext, isMultifactorAuthenticationAllowed(requestContext));
        PasswordlessWebflowUtils.putDelegatedAuthenticationAllowed(requestContext, isDelegatedAuthenticationAllowed(requestContext));
        PasswordlessWebflowUtils.putPasswordlessAuthenticationEnabled(requestContext, isPasswordlessAuthenticationAllowed(user));
        return null;
    }

    protected boolean isPasswordlessAuthenticationAllowed(final PasswordlessUserAccount user) {
        return user.hasContactInformation() && communicationsManager.isCommunicationChannelAvailable();
    }

    protected boolean isDelegatedAuthenticationAllowed(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val producer = providerConfigurationProducer.getIfAvailable();
        return isDelegatedAuthenticationActiveFor(requestContext, user) && producer != null && !producer.produce(requestContext).isEmpty();
    }

    protected boolean isMultifactorAuthenticationAllowed(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val mfaTriggersAvailable = !multifactorTriggerSelectionStrategy.getMultifactorAuthenticationTriggers().isEmpty();
        return mfaTriggersAvailable
            && shouldActivateMultifactorAuthenticationFor(requestContext, user)
            && isMultifactorAuthenticationPossible(requestContext).isPresent();
    }

    protected Optional<MultifactorAuthenticationProvider> isMultifactorAuthenticationPossible(final RequestContext requestContext) throws Throwable {
        val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val authentication = buildAuthentication(user);
        val service = WebUtils.getService(requestContext);
        return resolveMultifactorAuthenticationProvider(requestContext, authentication, service);
    }
}
