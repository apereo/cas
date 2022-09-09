package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.NonNull;
import lombok.val;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link DefaultAuthenticationSystemSupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public record DefaultAuthenticationSystemSupport(AuthenticationTransactionManager authenticationTransactionManager, PrincipalElectionStrategy principalElectionStrategy,
                                                 AuthenticationResultBuilderFactory authenticationResultBuilderFactory, AuthenticationTransactionFactory authenticationTransactionFactory)
    implements AuthenticationSystemSupport {

    @Override
    public AuthenticationResultBuilder handleInitialAuthenticationTransaction(final Service service,
                                                                              final Credential... credential) throws AuthenticationException {
        val builder = authenticationResultBuilderFactory.newBuilder();
        if (credential != null) {
            Stream.of(credential).filter(Objects::nonNull).forEach(builder::collect);
        }
        return this.handleAuthenticationTransaction(service, builder, credential);
    }

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication,
                                                                                 final Credential credentials) {
        return establishAuthenticationContextFromInitial(authentication).collect(credentials);
    }

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication) {
        return authenticationResultBuilderFactory.newBuilder().collect(authentication);
    }

    @Override
    public AuthenticationResultBuilder handleAuthenticationTransaction(final Service service,
                                                                       final AuthenticationResultBuilder authenticationResultBuilder,
                                                                       final Credential... credentials) throws AuthenticationException {

        val transaction = authenticationTransactionFactory.newTransaction(service, credentials);
        transaction.collect(authenticationResultBuilder.getAuthentications());
        authenticationTransactionManager.handle(transaction, authenticationResultBuilder);
        return authenticationResultBuilder;
    }

    @Override
    public AuthenticationResult finalizeAllAuthenticationTransactions(
        @NonNull
        final AuthenticationResultBuilder authenticationResultBuilder,
        final Service service) {
        return authenticationResultBuilder.build(principalElectionStrategy, service);
    }

    @Override
    public AuthenticationResult finalizeAuthenticationTransaction(final Service service, final Credential... credential)
        throws AuthenticationException {

        return finalizeAllAuthenticationTransactions(handleInitialAuthenticationTransaction(service, credential), service);
    }
}
