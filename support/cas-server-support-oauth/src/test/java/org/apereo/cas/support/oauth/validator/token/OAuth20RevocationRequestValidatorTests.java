package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20RevocationRequestValidatorTests}.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Tag("OAuth")
class OAuth20RevocationRequestValidatorTests extends AbstractOAuth20Tests {
    private static final String SUPPORTING_SERVICE_TICKET = "RT-SUPPORTING";

    @Autowired
    @Qualifier("oauthRevocationRequestValidator")
    private OAuth20TokenRequestValidator validator;

    private OAuthRegisteredService supportingService;

    @BeforeEach
    void before() throws Throwable {
        servicesManager.deleteAll();
        supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        servicesManager.save(supportingService);
        registerTicket(SUPPORTING_SERVICE_TICKET);

    }

    @Test
    void verifyOperationClientSecretPost() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);

        assertTrue(validator.validate(new JEEContext(request, response)));

        request.removeAllParameters();
        request.setParameter(OAuth20Constants.CLIENT_ID, supportingService.getClientId());
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        assertFalse(validator.supports(new JEEContext(request, response)));

        request.removeAllParameters();
        request.setParameter(OAuth20Constants.CLIENT_ID, "unknown");
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));
    }

    @Test
    void verifyOperationClientSecretBasic() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        HttpUtils.createBasicAuthHeaders(supportingService.getClientId(), RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertTrue(validator.validate(new JEEContext(request, response)));

        request.removeHeader(HttpHeaders.AUTHORIZATION);
        request.removeAllParameters();
        HttpUtils.createBasicAuthHeaders(supportingService.getClientId(), RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        assertFalse(validator.supports(new JEEContext(request, response)));

        request.removeHeader(HttpHeaders.AUTHORIZATION);
        request.removeAllParameters();
        HttpUtils.createBasicAuthHeaders("unknown", RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertFalse(validator.validate(new JEEContext(request, response)));
    }

    private void registerTicket(final String name) throws Throwable {
        val oauthCode = mock(OAuth20RefreshToken.class);
        when(oauthCode.getId()).thenReturn(name);
        when(oauthCode.isExpired()).thenReturn(false);
        when(oauthCode.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(oauthCode.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(oauthCode);
    }
}
