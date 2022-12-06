package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.Optional;

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

    private final ConfigurableApplicationContext applicationContext;


    public AcceptPasswordlessAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                  final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                  final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                  final PasswordlessTokenRepository passwordlessTokenRepository,
                                                  final AuthenticationSystemSupport authenticationSystemSupport,
                                                  final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                  final ConfigurableApplicationContext applicationContext) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val principal = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        try {
            val token = requestContext.getRequestParameters().getRequired("token");
            val passwordlessToken = passwordlessTokenRepository.findToken(principal.getUsername()).orElseThrow();
            return FunctionUtils.doIf(passwordlessToken.getToken().equalsIgnoreCase(token),
                    () -> {
                        handlePasswordlessAuthenticationAttempt(requestContext, principal, passwordlessToken);
                        val finalEvent = super.doExecute(requestContext);
                        passwordlessTokenRepository.deleteToken(passwordlessToken);
                        return finalEvent;
                    }, () -> {
                        throw new AuthenticationException("Provided token " + token + " is not issued by and does not belong to " + principal.getUsername());
                    })
                .get();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            val attributes = new LocalAttributeMap<>();
            attributes.put("error", e);
            var account = principal != null ? passwordlessUserAccountStore.findUser(principal.getUsername()) : Optional.empty();
            account.ifPresent(o -> attributes.put("passwordlessAccount", o));
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
        }
    }

    protected void handlePasswordlessAuthenticationAttempt(final RequestContext requestContext, final PasswordlessUserAccount principal,
                                                           final PasswordlessAuthenticationToken token) {
        val credential = new OneTimePasswordCredential(principal.getUsername(), token.getToken());
        val service = WebUtils.getService(requestContext);
        var authenticationResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);

        val processors = new ArrayList<>(applicationContext.getBeansOfType(PasswordlessAuthenticationPreProcessor.class).values());
        AnnotationAwareOrderComparator.sortIfNecessary(processors);
        for (val processor : processors) {
            authenticationResultBuilder = processor.process(authenticationResultBuilder, principal, service, credential, token);
        }
        val authenticationResult = authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
        WebUtils.putCredential(requestContext, credential);
    }
}
