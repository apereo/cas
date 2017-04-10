package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BaseAuthenticationMetadataPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link SurrogateAuthenticationMetadataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateAuthenticationMetadataPopulator extends BaseAuthenticationMetadataPopulator {
    /**
     * Surrogate username attribute in the authentication payload.
     */
    public static final String AUTHENTICATION_ATTR_SURROGATE_USER = "surrogateUser";
    /**
     * Original credential attribute in the authentication payload.
     */
    public static final String AUTHENTICATION_ATTR_SURROGATE_CREDENTIAL = "surrogateCredential";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationMetadataPopulator.class);
    
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final SurrogateUsernamePasswordCredential current = SurrogateUsernamePasswordCredential.class.cast(transaction.getCredential());
        LOGGER.debug("Recording surrogate username [{}] as an authentication attribute", current.getSurrogateUsername());
        builder.addAttribute(AUTHENTICATION_ATTR_SURROGATE_USER, current.getSurrogateUsername());
        builder.addAttribute(AUTHENTICATION_ATTR_SURROGATE_CREDENTIAL, current.getId());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && SurrogateUsernamePasswordCredential.class.isAssignableFrom(credential.getClass());
    }
}
