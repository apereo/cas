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
public class InweboAuthenticationDeviceMetadataPopulator extends BaseAuthenticationMetaDataPopulator {

    /**
     * Authentication attribute to capture device name.
     */
    public static final String ATTRIBUTE_NAME_INWEBO_AUTHENTICATION_DEVICE = "inweboAuthenticationDevice";

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        val inweboCredential = (InweboCredential) transaction.getCredentials().iterator().next();
        builder.addAttribute(ATTRIBUTE_NAME_INWEBO_AUTHENTICATION_DEVICE, inweboCredential.getDeviceName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return InweboCredential.class.isAssignableFrom(credential.getClass());
    }
}
