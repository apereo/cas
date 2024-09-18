package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link SurrogateAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    private final SurrogateAuthenticationService surrogateAuthenticationService;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        val primaryCredential = transaction.getPrimaryCredential();
        primaryCredential.ifPresentOrElse(credential -> {
            val surrogateUsername = credential.getCredentialMetadata()
                .getTrait(SurrogateCredentialTrait.class)
                .map(SurrogateCredentialTrait::getSurrogateUsername)
                .orElseThrow();
            surrogateAuthenticationService.collectSurrogateAttributes(builder, surrogateUsername, credential.getId());
        }, () -> {
            throw new SurrogateAuthenticationException("The authentication transaction does not have a primary principal");
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getCredentialMetadata()
            .getTrait(SurrogateCredentialTrait.class)
            .stream()
            .anyMatch(trait -> StringUtils.isNotBlank(trait.getSurrogateUsername()));
    }
}
