package org.apereo.cas.ticket;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.HashMap;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseOAuth20ExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AbstractOAuth20Tests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseOAuth20ExpirationPolicyTests {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);

    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected OAuth20RefreshTokenFactory defaultRefreshTokenFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING)
    protected TicketTrackingPolicy descendantTicketsTrackingPolicy;

    protected static TicketGrantingTicket newTicketGrantingTicket() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        return new TicketGrantingTicketImpl(
            ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
            CoreAuthenticationTestUtils.getAuthentication(principal),
            EXP_POLICY_TGT);
    }

    protected OAuth20Code createOAuthCode() throws Throwable {
        val builder = mock(ExpirationPolicyBuilder.class);
        when(builder.buildTicketExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(), builder,
            servicesManager, CipherExecutor.noOpOfStringToString(), descendantTicketsTrackingPolicy)
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), "code-challenge",
                "code-challenge-method", "clientId1234567", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }

    protected OAuth20AccessToken newAccessToken(final TicketGrantingTicket tgt) throws Throwable {
        val code = createOAuthCode();
        val testService = CoreAuthenticationTestUtils.getService("https://service.example.com");
        return defaultAccessTokenFactory.create(testService, tgt.getAuthentication(),
            tgt, new ArrayList<>(), code.getId(), code.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }

    protected OAuth20RefreshToken newRefreshToken(final OAuth20AccessToken at) throws Throwable {
        val testService = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val rt = defaultRefreshTokenFactory.create(testService, at.getAuthentication(),
            at.getTicketGrantingTicket(), new ArrayList<>(), "clientid12345", at.getId(),
            new HashMap<>(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ((TicketGrantingTicket) at.getTicketGrantingTicket()).getDescendantTickets().add(rt.getId());
        return rt;
    }
}
