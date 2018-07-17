package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.RememberMeCredential;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Determines if the credential provided are for Remember Me Services and then sets the appropriate
 * Authentication attribute if remember me services have been requested.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Slf4j
@ToString(callSuper = true)
public class RememberMeAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(r -> {
            if (RememberMeCredential.class.cast(r).isRememberMe()) {
                LOGGER.debug("Credential is configured to be remembered. Captured this as [{}] attribute",
                    RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
                builder.addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
            }
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeCredential;
    }
}
