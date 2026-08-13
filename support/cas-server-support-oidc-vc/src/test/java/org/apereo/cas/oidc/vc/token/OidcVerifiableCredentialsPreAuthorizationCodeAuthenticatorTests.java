package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcVerifiableCredentialsPreAuthorizationCodeAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
@TestPropertySource(properties = "cas.authn.attribute-repository.stub.attributes.email=casuser@example.org")
public class OidcVerifiableCredentialsPreAuthorizationCodeAuthenticatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator")
    private Authenticator oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator;

    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService transactionService;

    @Test
    void verifyValidPreAuthorizationCodeWithoutTransactionCode() {
        val credentialConfigurationIds = List.of("UniversityDegreeCredential", "DriverLicenseCredential");
        val ticket = issuePreAuthorizationCode(credentialConfigurationIds);
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.validate(createCallContext(null), credentials);
        assertTrue(result.isPresent());
        assertSame(credentials, result.orElseThrow());

        val profile = credentials.getUserProfile();
        assertNotNull(profile);
        assertEquals("casuser", profile.getId());
        assertEquals(getOidcRegisteredService().getClientId(), profile.getAttribute(OAuth20Constants.CLIENT_ID));
        assertEquals(credentialConfigurationIds, profile.getAttribute("credentialConfigurationIds"));
        assertNotNull(profile.getAttribute("email"));
    }

    @Test
    void verifyMatchingTransactionCodeIsCaseInsensitive() {
        val ticket = issuePreAuthorizationCode(List.of("UniversityDegreeCredential"));
        val transactionCode = ticket.getPropertyAsString("transactionId");
        assertNotNull(transactionCode);
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.validate(
            createCallContext(transactionCode.toLowerCase(Locale.ROOT)), credentials);
        assertTrue(result.isPresent());
        assertNotNull(credentials.getUserProfile());
    }

    @Test
    void verifyMismatchedTransactionCode() {
        val ticket = issuePreAuthorizationCode(List.of("UniversityDegreeCredential"));
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.validate(
            createCallContext(UUID.randomUUID().toString()), credentials);
        assertTrue(result.isEmpty());
        assertNull(credentials.getUserProfile());
    }

    @Test
    void verifyUnsupportedGrantType() {
        val credentials = createCredentials(UUID.randomUUID().toString(), OAuth20GrantTypes.AUTHORIZATION_CODE);
        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.validate(createCallContext(null), credentials);
        assertTrue(result.isEmpty());
        assertNull(credentials.getUserProfile());
    }

    @Test
    void verifyUnknownPreAuthorizationCode() {
        val credentials = createCredentials(TransientSessionTicket.PREFIX + UUID.randomUUID(),
            OAuth20GrantTypes.PRE_AUTHORIZED_CODE);
        assertThrows(NullPointerException.class,
            () -> oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.validate(createCallContext(null), credentials));
    }

    private TransientSessionTicket issuePreAuthorizationCode(final List<String> credentialConfigurationIds) {
        val registeredService = getOidcRegisteredService();
        val transaction = (TransientSessionTicket) transactionService.issue(
            registeredService.getClientId(), "casuser", credentialConfigurationIds);
        assertNotNull(transaction);

        val preAuthorizationCode = transaction.getProperty("preAuthorizedCode", String.class);
        assertNotNull(preAuthorizationCode);
        val ticket = (TransientSessionTicket) ticketRegistry.getTicket(preAuthorizationCode);
        assertNotNull(ticket);
        return ticket;
    }

    private static UsernamePasswordCredentials createCredentials(final String preAuthorizationCode,
                                                                  final OAuth20GrantTypes grantType) {
        return new UsernamePasswordCredentials(preAuthorizationCode, grantType.getType());
    }

    private static CallContext createCallContext(final String transactionCode) {
        val request = new MockHttpServletRequest();
        if (transactionCode != null) {
            request.addParameter(OidcConstants.TX_CODE, transactionCode);
        }
        val context = new JEEContext(request, new MockHttpServletResponse());
        return new CallContext(context, new JEESessionStore());
    }
}
