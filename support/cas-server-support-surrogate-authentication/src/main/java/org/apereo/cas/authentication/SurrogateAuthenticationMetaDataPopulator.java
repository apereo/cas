package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link SurrogateAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationMetaDataPopulator.class);
    
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final SurrogateUsernamePasswordCredential current = SurrogateUsernamePasswordCredential.class.cast(transaction.getCredential());
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
