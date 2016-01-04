package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.RememberMeCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Determines if the credential provided are for Remember Me Services and then sets the appropriate
 * Authentication attribute if remember me services have been requested.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Component("rememberMeAuthenticationMetaDataPopulator")
public final class RememberMeAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeAuthenticationMetaDataPopulator.class);

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final RememberMeCredential r = (RememberMeCredential) credential;
        if (r.isRememberMe()) {
            LOGGER.debug("Credential is configured to be remembered. Captured this as {} attribute",
                    RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeCredential;
    }
}
