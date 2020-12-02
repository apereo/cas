package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * The Inwebo metadata populator which adds the authentication device as an authentication attribute.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class AuthenticationDeviceMetadataPopulator extends BaseAuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        val inweboCredential = (InweboCredential) transaction.getCredentials().iterator().next();
        builder.addAttribute("inweboAuthenticationDevice", inweboCredential.getDeviceName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return InweboCredential.class.isAssignableFrom(credential.getClass());
    }
}
