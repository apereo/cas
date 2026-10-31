package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.validator.token.OAuth20InvalidTargetException;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
@ImportAutoConfiguration(CasOidcVerifiableCredentialsAutoConfiguration.class)
class OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidatorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator")
    private OAuth20TokenRequestValidator oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator;

    @Autowired
    @Qualifier(OidcVerifiableCredentialTransactionService.BEAN_NAME)
    private OidcVerifiableCredentialTransactionService transactionService;

    @Test
    void verifySupportsOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.supports(request, response));

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType());
        assertTrue(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.supports(request, response));
        assertEquals(Ordered.LOWEST_PRECEDENCE,
            oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.getOrder());
    }

    @Test
    void verifyValidPreAuthorizationCode() throws Throwable {
        val preAuthorizationCode = issuePreAuthorizationCode();
        val ticket = (TransientSessionTicket) transactionService.fetchPreAuthorizationCode(preAuthorizationCode);
        val request = createRequest(preAuthorizationCode,
            ticket.getPropertyAsString(OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE));
        assertTrue(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyRequestedResources() throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setAllowedResources(Set.of("https://api.example.org/"));
        servicesManager.save(registeredService);
        val transaction = (TransientSessionTicket) transactionService.issue(
            registeredService.getClientId(), "casuser", List.of("UniversityDegreeCredential"));
        val preAuthorizationCode = Objects.requireNonNull(transaction).getProperty("preAuthorizedCode", String.class);
        val request = createRequest(preAuthorizationCode,
            transaction.getPropertyAsString(OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE));
        request.addParameter(OAuth20Constants.RESOURCE, "https://api.example.org/");
        assertTrue(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
        request.setParameter(OAuth20Constants.RESOURCE, "https://other.example.org/");
        assertThrows(OAuth20InvalidTargetException.class,
            () -> oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyMissingTransactionCodeIsRejected() throws Throwable {
        val preAuthorizationCode = issuePreAuthorizationCode();
        val request = createRequest(preAuthorizationCode);
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyTransactionIdIsNotAcceptedAsTransactionCode() throws Throwable {
        val registeredService = getOidcRegisteredService();
        val transaction = (TransientSessionTicket) transactionService.issue(
            registeredService.getClientId(), "casuser", List.of("UniversityDegreeCredential"));
        assertNotNull(transaction);
        val preAuthorizationCode = transaction.getProperty("preAuthorizedCode", String.class);
        assertNotNull(preAuthorizationCode);

        val transactionCode = transaction.getPropertyAsString(
            OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE);
        assertNotNull(transactionCode);
        assertNotEquals(transaction.getId(), transactionCode);

        val request = createRequest(preAuthorizationCode, transaction.getId());
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyInvalidTransactionCode() throws Throwable {
        val preAuthorizationCode = issuePreAuthorizationCode();
        val request = createRequest(preAuthorizationCode, "invalid-transaction-code");
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyUnknownPreAuthorizationCode() throws Throwable {
        val request = createRequest(TransientSessionTicket.PREFIX + UUID.randomUUID());
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyExpiredPreAuthorizationCode() throws Throwable {
        val preAuthorizationCode = TransientSessionTicket.PREFIX + UUID.randomUUID();
        val ticket = mock(TransientSessionTicket.class);
        when(ticket.isExpired()).thenReturn(true);
        val service = mock(OidcVerifiableCredentialTransactionService.class);
        when(service.fetchPreAuthorizationCode(preAuthorizationCode)).thenReturn(ticket);
        val validator = new OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator(
            applicationContext.getBeanProvider(OidcConfigurationContext.class), service);

        val request = createRequest(preAuthorizationCode);
        assertFalse(validator.validate(request, new MockHttpServletResponse()));
        verify(service).fetchPreAuthorizationCode(preAuthorizationCode);
    }

    @Test
    void verifyMissingPreAuthorizationCode() throws Throwable {
        val request = createRequest(null);
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(request, new MockHttpServletResponse()));
    }

    private String issuePreAuthorizationCode() {
        val registeredService = getOidcRegisteredService();
        val transaction = (TransientSessionTicket) transactionService.issue(
            registeredService.getClientId(), "casuser", List.of("UniversityDegreeCredential"));
        assertNotNull(transaction);
        val preAuthorizationCode = transaction.getProperty("preAuthorizedCode", String.class);
        assertNotNull(preAuthorizationCode);
        return preAuthorizationCode;
    }

    private MockHttpServletRequest createRequest(@Nullable final String preAuthorizationCode) {
        return createRequest(preAuthorizationCode, null);
    }

    private MockHttpServletRequest createRequest(@Nullable final String preAuthorizationCode,
                                     @Nullable final String transactionCode) {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType());
        if (preAuthorizationCode != null) {
            request.addParameter(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizationCode);
        }
        if (transactionCode != null) {
            request.addParameter(OidcConstants.TX_CODE, transactionCode);
        }
        val profile = new DefaultOAuth20UserProfile("casuser");
        storeProfileIntoSession(request, profile);
        return request;
    }

}
