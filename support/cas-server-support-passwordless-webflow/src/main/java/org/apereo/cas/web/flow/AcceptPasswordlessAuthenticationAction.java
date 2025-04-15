package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.stream.Collectors;

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
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val passwordlessUserAccount = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        try {
            val token = requestContext.getRequestParameters().getRequired("token");
            val passwordlessToken = passwordlessTokenRepository.findToken(passwordlessUserAccount.getUsername())
                .orElseThrow(() -> new AuthenticationException("Unable to find passwordless token for " + passwordlessUserAccount.getUsername()));
            if (passwordlessToken.getToken().equalsIgnoreCase(token)) {
                handlePasswordlessAuthenticationAttempt(requestContext, passwordlessUserAccount, passwordlessToken);
                val finalEvent = super.doExecuteInternal(requestContext);
                passwordlessTokenRepository.deleteToken(passwordlessToken);
                return finalEvent;
            }
            throw new AuthenticationException("Provided token " + token + " is not issued by and does not belong to " + passwordlessUserAccount.getUsername());
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
                                                           final PasswordlessAuthenticationToken token) throws Throwable {
        val credential = new OneTimePasswordCredential(principal.getUsername(), token.getToken());
        val service = WebUtils.getService(requestContext);
        var authenticationResultBuilder = authenticationSystemSupport.handleInitialAuthenticationTransaction(service, credential);

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
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
        WebUtils.putCredential(requestContext, credential);
    }
}
