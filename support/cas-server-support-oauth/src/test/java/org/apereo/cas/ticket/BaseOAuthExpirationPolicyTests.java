package org.apereo.cas.ticket;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthComponentSerializationConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.web.config.CasCookieConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.util.ArrayList;

/**
 * This is {@link BaseOAuthExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasOAuthConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasOAuthComponentSerializationConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class
})
public abstract class BaseOAuthExpirationPolicyTests {
    protected static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthTokenExpirationPolicy.json");
    protected static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator(64);
    private static final ExpirationPolicy EXP_POLICY_TGT = new HardTimeoutExpirationPolicy(1000);

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected RefreshTokenFactory defaultRefreshTokenFactory;

    protected TicketGrantingTicket newTicketGrantingTicket() {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        return new TicketGrantingTicketImpl(
            ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
            CoreAuthenticationTestUtils.getAuthentication(principal),
            EXP_POLICY_TGT);
    }

    protected AccessToken newAccessToken(final TicketGrantingTicket tgt) {
        val testService = CoreAuthenticationTestUtils.getService("https://service.example.com");
        return defaultAccessTokenFactory.create(testService, tgt.getAuthentication(), tgt, new ArrayList<>());
    }

    protected RefreshToken newRefreshToken(final AccessToken at) {
        val testService = CoreAuthenticationTestUtils.getService("https://service.example.com");
        val rt = defaultRefreshTokenFactory.create(testService, at.getAuthentication(),
            at.getTicketGrantingTicket(), new ArrayList<>());
        at.getTicketGrantingTicket().getDescendantTickets().add(rt.getId());
        return rt;
    }
}
