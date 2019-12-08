package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link ByCredentialSourceAuthenticationHandlerResolver}
 * that attempts to capture the source from the credential
 * and limit the handlers to the matching source.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ByCredentialSourceAuthenticationHandlerResolver implements AuthenticationHandlerResolver {

    @Override
    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        return transaction.hasCredentialOfType(UsernamePasswordCredential.class);
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        val finalHandlers = new LinkedHashSet<AuthenticationHandler>(candidateHandlers.size());
        val upcs = transaction.getCredentialsOfType(UsernamePasswordCredential.class);
        candidateHandlers
            .stream()
            .filter(handler -> handler.supports(UsernamePasswordCredential.class))
            .filter(handler -> {
                val handlerName = handler.getName();
                LOGGER.debug("Evaluating authentication handler [{}] for eligibility", handlerName);
                return upcs.stream().anyMatch(c -> {
                    LOGGER.debug("Comparing credential source [{}] against authentication handler [{}]", c.getSource(), handlerName);
                    return StringUtils.isNotBlank(c.getSource()) && c.getSource().equalsIgnoreCase(handlerName);
                });
            })
            .forEach(finalHandlers::add);
        return finalHandlers;
    }
}
