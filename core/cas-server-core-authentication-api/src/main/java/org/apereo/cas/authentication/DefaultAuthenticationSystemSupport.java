package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.services.ServicesManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link DefaultAuthenticationSystemSupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Getter
@RequiredArgsConstructor
public class DefaultAuthenticationSystemSupport implements AuthenticationSystemSupport {

    private final AuthenticationTransactionManager authenticationTransactionManager;

    private final PrincipalElectionStrategy principalElectionStrategy;

    private final AuthenticationResultBuilderFactory authenticationResultBuilderFactory;

    private final AuthenticationTransactionFactory authenticationTransactionFactory;

    private final ServicesManager servicesManager;

    private final PrincipalResolver principalResolver;

    private final PrincipalFactory principalFactory;

    private final TenantExtractor tenantExtractor;
    
    private final TenantsManager tenantsManager;

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication,
                                                                                 final Credential... credentials) {
        return establishAuthenticationContextFromInitial(authentication).collect(credentials);
    }

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication) {
        return authenticationResultBuilderFactory.newBuilder().collect(authentication);
    }

    @Override
    public AuthenticationResultBuilder handleInitialAuthenticationTransaction(final Service service,
                                                                              final Credential... credential) throws Throwable {
        val builder = authenticationResultBuilderFactory.newBuilder();
        if (credential != null) {
            Stream.of(credential).filter(Objects::nonNull).forEach(builder::collect);
        }
        return this.handleAuthenticationTransaction(service, builder, credential);
    }

    @Override
    public AuthenticationResultBuilder handleAuthenticationTransaction(final Service service,
                                                                       final AuthenticationResultBuilder authenticationResultBuilder,
                                                                       final Credential... credentials) throws Throwable {

        val transaction = authenticationTransactionFactory.newTransaction(service, credentials)
            .collect(authenticationResultBuilder.getAuthentications());
        authenticationTransactionManager.handle(transaction, authenticationResultBuilder);
        return authenticationResultBuilder;
    }

    @Override
    public AuthenticationResult finalizeAllAuthenticationTransactions(@NonNull final AuthenticationResultBuilder authenticationResultBuilder,
                                                                      final Service service) throws Throwable {
        return authenticationResultBuilder.build(service);
    }

    @Override
    public AuthenticationResult finalizeAuthenticationTransaction(final Service service, final Credential... credential) throws Throwable {
        return finalizeAllAuthenticationTransactions(handleInitialAuthenticationTransaction(service, credential), service);
    }
}
