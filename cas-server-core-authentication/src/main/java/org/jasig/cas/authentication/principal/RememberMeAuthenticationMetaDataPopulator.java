package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.RememberMeCredential;
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

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final RememberMeCredential r = (RememberMeCredential) credential;
        if (r.isRememberMe()) {
            builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeCredential;
    }
}
