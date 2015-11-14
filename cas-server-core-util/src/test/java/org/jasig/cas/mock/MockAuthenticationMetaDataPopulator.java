package org.jasig.cas.mock;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {}

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }

}
