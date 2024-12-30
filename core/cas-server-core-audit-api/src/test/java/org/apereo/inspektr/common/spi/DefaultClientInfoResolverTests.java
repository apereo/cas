package org.apereo.inspektr.common.spi;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultClientInfoResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Audits")
class DefaultClientInfoResolverTests {

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        val fp = UUID.randomUUID().toString();
        request.setParameter("deviceFingerprint", fp);
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val resolver = new DefaultClientInfoResolver();
        val resolved = resolver.resolveFrom(mock(JoinPoint.class), new Object());
        assertEquals(resolved.getDeviceFingerprint(), fp);
    }
}
