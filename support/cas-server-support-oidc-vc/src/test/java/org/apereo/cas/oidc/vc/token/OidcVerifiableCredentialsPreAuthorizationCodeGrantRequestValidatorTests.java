package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.config.CasOidcVerifiableCredentialsAutoConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
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
        val context = new JEEContext(request, new MockHttpServletResponse());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.supports(context));

        request.setParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType());
        assertTrue(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.supports(context));
        assertEquals(Ordered.LOWEST_PRECEDENCE,
            oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.getOrder());
    }

    @Test
    void verifyValidPreAuthorizationCode() throws Throwable {
        val preAuthorizationCode = issuePreAuthorizationCode();
        val context = createContext(preAuthorizationCode);
        assertTrue(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(context));
    }

    @Test
    void verifyUnknownPreAuthorizationCode() throws Throwable {
        val context = createContext(TransientSessionTicket.PREFIX + UUID.randomUUID());
        assertFalse(oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(context));
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

        val context = createContext(preAuthorizationCode);
        assertFalse(validator.validate(context));
        verify(service).fetchPreAuthorizationCode(preAuthorizationCode);
    }

    @Test
    void verifyMissingPreAuthorizationCode() {
        val context = createContext(null);
        assertThrows(NoSuchElementException.class,
            () -> oidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator.validate(context));
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

    private JEEContext createContext(@Nullable final String preAuthorizationCode) {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PRE_AUTHORIZED_CODE.getType());
        if (preAuthorizationCode != null) {
            request.addParameter(OidcConstants.PRE_AUTHORIZED_CODE, preAuthorizationCode);
        }
        val context = new JEEContext(request, new MockHttpServletResponse());

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId("casuser");
        new ProfileManager(context, oauthDistributedSessionStore).save(true, profile, false);
        return context;
    }

}
