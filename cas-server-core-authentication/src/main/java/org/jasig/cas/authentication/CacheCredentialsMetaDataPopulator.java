package org.jasig.cas.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * We utilize the {@link org.jasig.cas.authentication.AuthenticationMetaDataPopulator} to retrieve and store
 * the password as an authentication attribute under the key
 * {@link UsernamePasswordCredential#AUTHENTICATION_ATTRIBUTE_PASSWORD}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component("cacheCredentialsMetaDataPopulator")
public final class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        logger.debug("Processing request to capture the credential for [{}]", credential.getId());
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        builder.addAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, c.getPassword());
        logger.debug("Encrypted credential is added as the authentication attribute [{}] to the authentication",
                    UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);

    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
