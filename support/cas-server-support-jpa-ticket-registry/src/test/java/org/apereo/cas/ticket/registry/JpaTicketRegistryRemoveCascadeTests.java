 package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JpaTicketRegistry} class. Which demonstrates the correct removal of cascading tickets.
 *
 * @author Thomas Seliger
 * @since 7.0.0
 */
@Import(JpaTicketRegistryTests.SharedTestConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.jdbc.show-sql=false",
        "cas.ticket.registry.jpa.ddl-auto=create-drop",
        "cas.logout.remove-descendant-tickets=true"
    })
@Tag("JDBC")
@Getter
@EnableConfigurationProperties({IntegrationProperties.class, CasConfigurationProperties.class})
public class JpaTicketRegistryRemoveCascadeTests extends BaseTicketRegistryTests {
    
	@Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory oAuthCodeFactory;
	
    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory oAuthAccessTokenFactory;
    
    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    protected OAuth20RefreshTokenFactory oAuthRefreshTokenFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("dataSourceTicket")
    protected CloseableDataSource dataSourceTicket;

    @AfterAll
    public static void afterAllTests() throws Exception {
        ApplicationContextProvider.getApplicationContext()
            .getBean("dataSourceTicket", CloseableDataSource.class).close();
    }

    @AfterEach
    public void cleanup() throws Exception {
        assertNotNull(dataSourceTicket);
        newTicketRegistry.deleteAll();
    }
    
    @RepeatedTest(2)
    public void verifyLogoutCascadesCode() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        this.newTicketRegistry.addTicket(tgt);

        val oAuthCode = oAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            originalAuthn, tgt, Collections.emptySet(), "challenge", "challenge_method",
            "client_id", Collections.emptyMap(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.newTicketRegistry.addTicket(oAuthCode);
        this.newTicketRegistry.updateTicket(tgt);

        assertNotNull(this.newTicketRegistry.getTicket(oAuthCode.getId()));
        this.newTicketRegistry.deleteTicket(tgt.getId());
        assertNull(this.newTicketRegistry.getTicket(oAuthCode.getId()));
    }

    @RepeatedTest(2)
    public void verifyLogoutCascadesAccessToken() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
                RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        this.newTicketRegistry.addTicket(tgt);

        val oAuthAt = oAuthAccessTokenFactory.create(RegisteredServiceTestUtils.getService(),
                originalAuthn, tgt, Collections.emptySet(), "code", "client_id",
                Collections.emptyMap(), OAuth20ResponseTypes.TOKEN,
                OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.newTicketRegistry.addTicket(oAuthAt);
        this.newTicketRegistry.updateTicket(tgt);

        assertNotNull(this.newTicketRegistry.getTicket(oAuthAt.getId()));
        this.newTicketRegistry.deleteTicket(tgt.getId());
        assertNull(this.newTicketRegistry.getTicket(oAuthAt.getId()));
    }
    
    @RepeatedTest(2)
    public void verifyLogoutCascadesRefreshToken() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
                RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        this.newTicketRegistry.addTicket(tgt);

        val oAuthRt = oAuthRefreshTokenFactory.create(RegisteredServiceTestUtils.getService(),
                originalAuthn, tgt, Collections.emptySet(), "code", "client_id",
                Collections.emptyMap(), OAuth20ResponseTypes.TOKEN,
                OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.newTicketRegistry.addTicket(oAuthRt);
        this.newTicketRegistry.updateTicket(tgt);

        assertNotNull(this.newTicketRegistry.getTicket(oAuthRt.getId()));
        this.newTicketRegistry.deleteTicket(tgt.getId());
        assertNull(this.newTicketRegistry.getTicket(oAuthRt.getId()));
    }

}
