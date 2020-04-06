package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20RevocationRequestValidatorTests}.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20RevocationRequestValidatorTests {
    private static final String SUPPORTING_SERVICE_TICKET = "RT-SUPPORTING";

    private TicketRegistry ticketRegistry;
    private OAuth20TokenRequestValidator validator;

    private void registerTicket(final String name) {
        val oauthCode = mock(OAuth20RefreshToken.class);
        when(oauthCode.getId()).thenReturn(name);
        when(oauthCode.isExpired()).thenReturn(false);
        when(oauthCode.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(ticketRegistry.getTicket(eq(name))).thenReturn(oauthCode);
    }

    @BeforeEach
    public void before() {
        val servicesManager = mock(ServicesManager.class);

        val supportingService = RequestValidatorTestUtils.getService(
            RegisteredServiceTestUtils.CONST_TEST_URL,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SUPPORTING_CLIENT_ID,
            RequestValidatorTestUtils.SHARED_SECRET,
            CollectionUtils.wrapSet(OAuth20GrantTypes.REFRESH_TOKEN));
        when(servicesManager.getAllServices()).thenReturn(CollectionUtils.wrapList(supportingService));

        this.ticketRegistry = mock(TicketRegistry.class);

        registerTicket(SUPPORTING_SERVICE_TICKET);

        this.validator = new OAuth20RevocationRequestValidator(servicesManager);
    }

    @Test
    public void verifyOperationClientSecretPost() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);

        assertTrue(this.validator.validate(new JEEContext(request, response)));

        request.removeAllParameters();
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        assertFalse(this.validator.supports(new JEEContext(request, response)));

        request.removeAllParameters();
        request.setParameter(OAuth20Constants.CLIENT_ID, RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID);
        request.setParameter(OAuth20Constants.CLIENT_SECRET, RequestValidatorTestUtils.SHARED_SECRET);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertFalse(this.validator.validate(new JEEContext(request, response)));
    }

    @Test
    public void verifyOperationClientSecretBasic() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        HttpUtils.createBasicAuthHeaders(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID, RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertTrue(this.validator.validate(new JEEContext(request, response)));


        request.removeHeader("Authorization");
        request.removeAllParameters();
        HttpUtils.createBasicAuthHeaders(RequestValidatorTestUtils.SUPPORTING_CLIENT_ID, RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        assertFalse(this.validator.supports(new JEEContext(request, response)));

        request.removeHeader("Authorization");
        request.removeAllParameters();
        HttpUtils.createBasicAuthHeaders(RequestValidatorTestUtils.NON_SUPPORTING_CLIENT_ID, RequestValidatorTestUtils.SHARED_SECRET).forEach(request::addHeader);
        request.setParameter(OAuth20Constants.TOKEN, SUPPORTING_SERVICE_TICKET);
        assertFalse(this.validator.validate(new JEEContext(request, response)));
    }
}
