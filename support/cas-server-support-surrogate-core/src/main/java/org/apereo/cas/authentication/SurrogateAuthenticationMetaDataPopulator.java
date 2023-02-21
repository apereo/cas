package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;

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
public class SurrogateAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        val primaryCredential = transaction.getPrimaryCredential();
        primaryCredential.ifPresentOrElse(credential -> {
            val surrogateUsername = credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
                .map(SurrogateCredentialTrait::getSurrogateUsername)
                .orElseThrow();
            LOGGER.debug("Recording surrogate username [{}] as an authentication attribute", surrogateUsername);
            builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, surrogateUsername);
            builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, credential.getId());
            builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, Boolean.TRUE.toString());
        }, () -> {
            throw new SurrogateAuthenticationException("The authentication transaction does not have a primary principal");
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .stream()
            .anyMatch(trait -> StringUtils.isNotBlank(trait.getSurrogateUsername()));
    }
}
