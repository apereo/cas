package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.X509Credentials;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20X509AuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OAuth")
public class OAuth20X509AuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthX509CertificateAuthenticator")
    private Authenticator authenticator;

    @Test
    void verifyAcceptedCredentialsWithClientId() {
        val certificate = new CasX509Certificate(true);
        val credentials = new X509Credentials(certificate);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "TlsWithClientId");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    void verifyAcceptedCredentialsWitSpiffe() {
        val certificate = new CasX509Certificate(true)
            .setSubjectAltNames(List.of("spiffe://example.org/client"));
        val credentials = new X509Credentials(certificate);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "TlsWithClientId");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
    }
}
