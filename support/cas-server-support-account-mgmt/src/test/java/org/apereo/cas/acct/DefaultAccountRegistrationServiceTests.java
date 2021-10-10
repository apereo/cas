package org.apereo.cas.acct;

import org.apereo.cas.acct.provision.AccountRegistrationProvisioner;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAccountRegistrationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
},
    properties = "cas.server.prefix=https://sso.example.org/cas")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DefaultAccountRegistrationServiceTests {
    private AccountRegistrationService accountRegistrationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void setup() {
        this.accountRegistrationService = new DefaultAccountRegistrationService(
            mock(AccountRegistrationPropertyLoader.class),
            casProperties, CipherExecutor.noOpOfSerializableToString(),
            AccountRegistrationUsernameBuilder.asDefault(),
            mock(AccountRegistrationProvisioner.class));

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("1.2.3.4");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyMissingIssuer() throws Exception {
        val claims = new JwtClaims();
        claims.setIssuer("unknown");
        val token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));
    }

    @Test
    public void verifyMissingAudience() throws Exception {
        val claims = new JwtClaims();
        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience("unknown");
        val token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));
    }

    @Test
    public void verifyMissingSubject() throws Exception {
        val claims = new JwtClaims();
        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience(casProperties.getServer().getPrefix());
        val token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));
    }

    @Test
    public void verifyMissingClientInfo() throws Exception {
        val claims = new JwtClaims();
        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience(casProperties.getServer().getPrefix());
        claims.setClaim("origin", "2.2.3.4");
        claims.setSubject("casuser");
        var token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));

        claims.setClaim("origin", "1.2.3.4");
        claims.setClaim("client", "0.2.3.4");
        token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));

        val exp = NumericDate.now();
        exp.addSeconds(-60);
        claims.setExpirationTime(exp);
        claims.setClaim("client", "1.2.3.4");
        token = claims.toJson();
        assertNull(accountRegistrationService.validateToken(token));
    }
}
