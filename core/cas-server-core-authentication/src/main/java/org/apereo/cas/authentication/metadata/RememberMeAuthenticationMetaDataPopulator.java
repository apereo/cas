package org.apereo.cas.authentication.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.RememberMeCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines if the credential provided are for Remember Me Services and then sets the appropriate
 * Authentication attribute if remember me services have been requested.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
public class RememberMeAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeAuthenticationMetaDataPopulator.class);

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final RememberMeCredential r = (RememberMeCredential) transaction.getCredential();
        if (r.isRememberMe()) {
            LOGGER.debug("Credential is configured to be remembered. Captured this as [{}] attribute",
                    RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
        }
    }

    
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeCredential;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
