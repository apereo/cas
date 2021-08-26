package org.apereo.cas.pm.impl;

import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NoOpPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("PasswordOps")
public class NoOpPasswordManagementServiceTests {

    @Test
    public void verifyChange() {
        val properties = new PasswordManagementProperties();
        val service = new NoOpPasswordManagementService(CipherExecutor.noOpOfSerializableToString(), "CAS", properties);
        assertFalse(service.changeInternal(
            RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"),
            new PasswordChangeRequest()));
    }


    @Test
    public void verifyTokenParsing() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        val clientInfo = new ClientInfo(request);
        ClientInfoHolder.setClientInfo(clientInfo);

        val properties = new PasswordManagementProperties();
        val service = new NoOpPasswordManagementService(CipherExecutor.noOpOfSerializableToString(), "CAS", properties);
        val token = UUID.randomUUID().toString();
        val claims = new JwtClaims();
        claims.setJwtId(token);

        claims.setIssuer("bad-issuer");
        assertNull(service.parseToken(claims.toJson()));

        claims.setIssuer("CAS");
        claims.setAudience("other-audience");
        assertNull(service.parseToken(claims.toJson()));

        claims.setAudience("CAS");
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
