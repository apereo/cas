package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.web.flow.actions.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
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
    protected Event doExecute(final RequestContext requestContext) {
        val principal = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        try {
            val token = requestContext.getRequestParameters().get("token");
            val currentToken = passwordlessTokenRepository.findToken(principal.getUsername());

            if (currentToken.isPresent() && token.equalsIgnoreCase(currentToken.get())) {
                val credential = new OneTimePasswordCredential(principal.getUsername(), token);
                val service = WebUtils.getService(requestContext);
                val authenticationResult = authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
                WebUtils.putAuthenticationResult(authenticationResult, requestContext);
                WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
                WebUtils.putCredential(requestContext, credential);
                val finalEvent = super.doExecute(requestContext);
                passwordlessTokenRepository.deleteToken(principal.getUsername(), currentToken.get());
                return finalEvent;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            val attributes = new LocalAttributeMap<>();
            attributes.put("error", e);
            val account = passwordlessUserAccountStore.findUser(principal.getUsername());
            if (account.isPresent()) {
                attributes.put("passwordlessAccount", account.get());
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
            }
        }
        LOGGER.error("Unable to locate token for user [{}]", principal.getUsername());
        val attributes = new LocalAttributeMap<>();
        attributes.put("error", new AuthenticationException("Invalid token"));
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
    }
}
