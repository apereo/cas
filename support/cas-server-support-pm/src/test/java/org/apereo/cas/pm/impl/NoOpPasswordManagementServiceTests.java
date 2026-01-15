package org.apereo.cas.pm.impl;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NoOpPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("PasswordOps")
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.server.name=https://sso.example.org",
    "cas.server.prefix=${cas.server.name}/cas"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class NoOpPasswordManagementServiceTests {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Test
    void verifyChange() {
        val service = new NoOpPasswordManagementService(CipherExecutor.noOpOfSerializableToString(), casProperties);
        assertFalse(service.changeInternal(new PasswordChangeRequest()));
    }

    @Test
    void verifyTokenParsing() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        val clientInfo = ClientInfo.from(request);
        ClientInfoHolder.setClientInfo(clientInfo);

        val service = new NoOpPasswordManagementService(CipherExecutor.noOpOfSerializableToString(), casProperties);
        val token = UUID.randomUUID().toString();
        val claims = new JwtClaims();
        claims.setJwtId(token);

        claims.setIssuer("bad-issuer");
        assertNull(service.parseToken(claims.toJson()));

        claims.setIssuer(casProperties.getServer().getPrefix());
        claims.setAudience("other-audience");
        assertNull(service.parseToken(claims.toJson()));

        claims.setAudience(casProperties.getServer().getPrefix());
        claims.setSubject(StringUtils.EMPTY);
        assertNull(service.parseToken(claims.toJson()));

        claims.setClaim("origin", "whatever");
        claims.setSubject("casuser");
        assertNull(service.parseToken(claims.toJson()));

        claims.setClaim("origin", clientInfo.getServerIpAddress());
        claims.setClaim("client", "whatever");
        assertNull(service.parseToken(claims.toJson()));

        claims.setClaim("client", clientInfo.getClientIpAddress());
        val milli = Instant.now(Clock.systemUTC()).minusSeconds(500).toEpochMilli();
        claims.setExpirationTime(NumericDate.fromMilliseconds(milli));
        assertNull(service.parseToken(claims.toJson()));

        claims.setExpirationTime(NumericDate.now());
        assertNotNull(service.parseToken(claims.toJson()));
    }

}
