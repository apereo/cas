package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PasswordlessTokenCredential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AcceptPasswordlessAuthenticationAction extends AbstractAuthenticationAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    private final AuthenticationSystemSupport authenticationSystemSupport;


    public AcceptPasswordlessAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                  final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                  final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                  final PasswordlessTokenRepository passwordlessTokenRepository,
                                                  final AuthenticationSystemSupport authenticationSystemSupport,
                                                  final PasswordlessUserAccountStore passwordlessUserAccountStore) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val passwordlessUserAccount = Objects.requireNonNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class));
        try {
            val providedToken = requestContext.getRequestParameters().getRequired("token");
            val passwordlessToken = passwordlessTokenRepository.findToken(passwordlessUserAccount.getUsername()).orElse(null);
            handlePasswordlessAuthenticationAttempt(requestContext, passwordlessUserAccount, providedToken, passwordlessToken);
            val finalEvent = super.doExecuteInternal(requestContext);
            if (finalEvent != null && !CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE.equals(finalEvent.getId())
                && !passwordlessTokenRepository.deleteToken(Objects.requireNonNull(passwordlessToken))) {
                throw new AuthenticationException("Passwordless token for " + passwordlessUserAccount.getUsername() + " has already been used");
            }
            return finalEvent;
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            val attributes = new LocalAttributeMap<>();
            attributes.put("error", e);
            val request = PasswordlessAuthenticationRequest.builder()
                .username(passwordlessUserAccount.getUsername())
                .build();
            var account = passwordlessUserAccountStore.findUser(request);
            account.ifPresent(o -> attributes.put("passwordlessAccount", passwordlessUserAccount));
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
        }
    }

    protected void handlePasswordlessAuthenticationAttempt(final RequestContext requestContext, final PasswordlessUserAccount principal,
                                                           final String providedToken,
                                                           @Nullable final PasswordlessAuthenticationToken passwordlessToken)
            throws Throwable {
        val credential = new PasswordlessTokenCredential(principal.getUsername(), providedToken);
        val service = WebUtils.getService(requestContext);
        var authenticationResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);
        val token = Optional.ofNullable(passwordlessToken)
            .orElseThrow(() -> new AuthenticationException("Unable to find passwordless token for " + principal.getUsername()));

        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val processors = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, PasswordlessAuthenticationPreProcessor.class).values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());
        AnnotationAwareOrderComparator.sortIfNecessary(processors);
        for (val processor : processors) {
            authenticationResultBuilder = processor.process(authenticationResultBuilder, principal, service, credential, token);
        }
        val authenticationResult = authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        WebUtils.putAuthenticationResult(Objects.requireNonNull(authenticationResult), requestContext);
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
        WebUtils.putCredential(requestContext, credential);
    }
}
