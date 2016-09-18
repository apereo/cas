package org.apereo.cas.mock;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.AuthenticationBuilder;

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
