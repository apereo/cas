package org.apereo.cas.authentication.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We utilize the {@link AuthenticationMetaDataPopulator} to retrieve and store
 * the password as an authentication attribute under the key
 * {@link UsernamePasswordCredential#AUTHENTICATION_ATTRIBUTE_PASSWORD}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CacheCredentialsMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheCredentialsMetaDataPopulator.class);

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        LOGGER.debug("Processing request to capture the credential for [{}]", credential.getId());
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        builder.addAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, c.getPassword());
        LOGGER.debug("Encrypted credential is added as the authentication attribute [{}] to the authentication",
                    UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);

    }

    
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .toString();
    }
}
