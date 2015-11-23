package org.jasig.cas.support.pac4j.test;

import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.facebook.FacebookProfile;

/**
 * Mock class for the FacebookClient.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class MockFacebookClient extends FacebookClient {

    public static final String CLIENT_NAME = "FacebookClient";

    private FacebookProfile facebookProfile;

    @Override
    public String getName() {
        return CLIENT_NAME;
    }

    @Override
    protected void internalInit(final WebContext context) {
    }

    @Override
    protected OAuthCredentials retrieveCredentials(final WebContext context) {
        return new OAuthCredentials("fakeVerifier", getName());
    }

    @Override
    protected FacebookProfile retrieveUserProfile(final OAuthCredentials credentials, final WebContext context) {
        return facebookProfile;
    }

    public FacebookProfile getFacebookProfile() {
        return facebookProfile;
    }

    public void setFacebookProfile(final FacebookProfile facebookProfile) {
        this.facebookProfile = facebookProfile;
    }
}
