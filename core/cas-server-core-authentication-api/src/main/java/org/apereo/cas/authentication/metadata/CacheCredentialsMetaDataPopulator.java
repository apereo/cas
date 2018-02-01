package org.apereo.cas.authentication.metadata;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;

/**
 * We utilize the {@link AuthenticationMetaDataPopulator} to retrieve and store
 * the password as an authentication attribute under the key
 * {@link UsernamePasswordCredential#AUTHENTICATION_ATTRIBUTE_PASSWORD}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString(callSuper = true)
@AllArgsConstructor
public class CacheCredentialsMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    private final CipherExecutor<String, String> cipherExecutor;

    public CacheCredentialsMetaDataPopulator() {
        this(null);
        LOGGER.warn("No cipher is specified to handle credential caching encryption");
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final Credential credential = transaction.getCredential();
        LOGGER.debug("Processing request to capture the credential for [{}]", credential.getId());
        final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;
        final String psw = this.cipherExecutor == null ? c.getPassword() : this.cipherExecutor.encode(c.getPassword());
        builder.addAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD, psw);
        LOGGER.debug("Credential is added as the authentication attribute [{}] to the authentication",
            UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
