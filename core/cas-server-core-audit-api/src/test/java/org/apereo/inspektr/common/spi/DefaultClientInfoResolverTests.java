package org.apereo.inspektr.common.spi;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.val;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        val fp = UUID.randomUUID().toString();
        request.setParameter("deviceFingerprint", fp);
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val resolver = new DefaultClientInfoResolver();
        val resolved = resolver.resolveFrom(mock(JoinPoint.class), new Object());
        assertEquals(resolved.getDeviceFingerprint(), fp);
    }
}
