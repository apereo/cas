package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.AttributeRepositoryQuery;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolverUtils;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Optional;

/**
 * This is {@link SurrogateAuthenticationPrincipalBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultSurrogateAuthenticationPrincipalBuilder implements SurrogateAuthenticationPrincipalBuilder {

    private final PrincipalFactory principalFactory;

    private final PersonAttributeDao attributeRepository;

    private final SurrogateAuthenticationService surrogateAuthenticationService;

    private final AttributeRepositoryResolver attributeRepositoryResolver;

    private final CasConfigurationProperties casProperties;

    @Override
    public Principal buildSurrogatePrincipal(final Credential credential, final Principal primaryPrincipal,
                                             final RegisteredService registeredService) throws Throwable {

        val surrogate = extractSurrogateUser(credential);
        val activeAttributeRepositoryIdentifiers = PrincipalResolverUtils.buildActiveAttributeRepositoryIds(casProperties.getPersonDirectory());
        val query = AttributeRepositoryQuery.builder()
            .principal(primaryPrincipal)
            .activeRepositoryIds(activeAttributeRepositoryIdentifiers)
            .registeredService(registeredService)
            .tenant(credential.getTenant())
            .build();

        val repositoryIds = attributeRepositoryResolver.resolve(query);
        val attributes = PrincipalAttributeRepositoryFetcher
            .builder()
            .attributeRepository(attributeRepository)
            .principalId(surrogate)
            .activeAttributeRepositoryIdentifiers(repositoryIds)
            .currentPrincipal(primaryPrincipal)
            .build()
            .retrieve();

        val surrogatePrincipal = principalFactory.createPrincipal(surrogate, attributes);
        LOGGER.debug("Built surrogate principal [{}] with primary principal [{}]", surrogatePrincipal, primaryPrincipal);
        return new SurrogatePrincipal(primaryPrincipal, surrogatePrincipal);
    }


    @Override
    public Optional<AuthenticationResultBuilder> buildSurrogateAuthenticationResult(
        final AuthenticationResultBuilder authenticationResultBuilder,
        final Credential mutableCredential,
        final RegisteredService registeredService) throws Throwable {
        val initialAuthentication = authenticationResultBuilder.getInitialAuthentication();
        if (initialAuthentication.isPresent()) {
            val authentication = initialAuthentication.get();
            val principal = extractPrimaryPrincipal(authentication);

            val surrogateUsername = extractSurrogateUser(mutableCredential);
            if (!surrogateAuthenticationService.canImpersonate(surrogateUsername, principal, Optional.empty())) {
                throw new SurrogateAuthenticationException("Unable to authorize surrogate authentication request for " + surrogateUsername);
            }
            val surrogatePrincipal = buildSurrogatePrincipal(mutableCredential, principal, registeredService);
            val authenticationBuilder = DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(surrogatePrincipal);
            surrogateAuthenticationService.collectSurrogateAttributes(authenticationBuilder, surrogateUsername, principal.getId());
            return Optional.of(authenticationResultBuilder.collect(authenticationBuilder.build()));
        }
        return Optional.empty();
    }

    protected Principal extractPrimaryPrincipal(final Authentication authentication) {
        return authentication.getPrincipal() instanceof final SurrogatePrincipal surrogatePrincipal
            ? surrogatePrincipal.getPrimary()
            : authentication.getPrincipal();
    }

    protected String extractSurrogateUser(final Credential mutableCredential) {
        return mutableCredential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .map(SurrogateCredentialTrait::getSurrogateUsername)
            .orElseThrow(() -> new SurrogateAuthenticationException("Unable to locate surrogate credential"));
    }
}
