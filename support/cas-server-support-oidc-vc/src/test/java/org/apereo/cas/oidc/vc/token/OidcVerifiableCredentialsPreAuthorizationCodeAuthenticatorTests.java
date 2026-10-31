package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    private OAuth20ClientCredentialsAuthenticator oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator;

    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService transactionService;

    @Test
    void verifyValidPreAuthorizationCodeWithTransactionCode() {
        val credentialConfigurationIds = List.of("UniversityDegreeCredential", "DriverLicenseCredential");
        val ticket = issuePreAuthorizationCode(credentialConfigurationIds);
        val transactionCode = ticket.getPropertyAsString(
            OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE);
        assertNotNull(transactionCode);
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE, transactionCode);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.authenticate(credentials);
        assertTrue(result.isPresent());

        val profile = result.orElseThrow();
        assertEquals("casuser", profile.getId());
        assertEquals(getOidcRegisteredService().getClientId(), profile.getAttribute(OAuth20Constants.CLIENT_ID));
        assertEquals(credentialConfigurationIds, profile.getAttribute("credentialConfigurationIds"));
        assertNotNull(profile.getAttribute("email"));
    }

    @Test
    void verifyMissingTransactionCodeIsRejected() {
        val ticket = issuePreAuthorizationCode(List.of("UniversityDegreeCredential"));
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE, null);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.authenticate(credentials);
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyTransactionIdIsNotAcceptedAsTransactionCode() {
        val ticket = issuePreAuthorizationCode(List.of("UniversityDegreeCredential"));
        val transactionId = ticket.getPropertyAsString("transactionId");
        assertNotNull(transactionId);
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE, transactionId);

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.authenticate(credentials);
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyMismatchedTransactionCode() {
        val ticket = issuePreAuthorizationCode(List.of("UniversityDegreeCredential"));
        val credentials = createCredentials(ticket.getId(), OAuth20GrantTypes.PRE_AUTHORIZED_CODE, UUID.randomUUID().toString());

        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.authenticate(credentials);
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyUnsupportedGrantType() {
        val credentials = createCredentials(UUID.randomUUID().toString(), OAuth20GrantTypes.AUTHORIZATION_CODE, null);
        val result = oidcVerifiableCredentialsPreAuthorizationCodeAuthenticator.authenticate(credentials);
        assertTrue(result.isEmpty());
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

    private static OAuth20ClientAuthenticationToken createCredentials(final String preAuthorizationCode,
                                                                      final OAuth20GrantTypes grantType,
                                                                      final @Nullable String transactionCode) {
        val request = new MockHttpServletRequest();
        if (transactionCode != null) {
            request.addParameter(OidcConstants.TX_CODE, transactionCode);
        }
        val token = new OAuth20ClientAuthenticationToken(request, OAuth20ClientAuthenticationToken.CredentialSource.FORM,
            preAuthorizationCode, grantType.getType());
        token.setResponse(new MockHttpServletResponse());
        return token;
    }
}
