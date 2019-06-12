package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        val credential = transaction.getPrimaryCredential();
        if (credential.isEmpty()) {
            throw new SurrogateAuthenticationException("The authentication transaction does not have a primary principal associated with it");
        }

        val current = SurrogateUsernamePasswordCredential.class.cast(credential.get());
        LOGGER.debug("Recording surrogate username [{}] as an authentication attribute", current.getSurrogateUsername());
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, current.getSurrogateUsername());
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, current.getId());
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, Boolean.TRUE.toString());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && SurrogateUsernamePasswordCredential.class.isAssignableFrom(credential.getClass());
    }
}
