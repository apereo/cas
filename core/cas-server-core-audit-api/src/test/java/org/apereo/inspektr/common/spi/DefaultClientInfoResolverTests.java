package org.apereo.inspektr.common.spi;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoExtractionOptions;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import lombok.val;
import module java.base;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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

    @Test
    void verifyFilterRefusesTenantHeaderConflictingWithPath() throws Exception {
        val tenantExtractor = mock(TenantExtractor.class, CALLS_REAL_METHODS);
        doReturn(mock(TenantsManager.class)).when(tenantExtractor).getTenantsManager();
        val filter = new ClientInfoThreadLocalFilter(ClientInfoExtractionOptions.builder().build(), tenantExtractor);

        val conflictingRequest = new MockHttpServletRequest();
        conflictingRequest.setServletPath("/tenants/shire/login");
        conflictingRequest.addHeader(TenantExtractor.HEADER_TENANT_ID, "london");
        val conflictingResponse = new MockHttpServletResponse();
        val conflictingChain = new MockFilterChain();
        filter.doFilter(conflictingRequest, conflictingResponse, conflictingChain);
        assertEquals(HttpStatus.BAD_REQUEST.value(), conflictingResponse.getStatus());
        assertNull(conflictingChain.getRequest());

        val request = new MockHttpServletRequest();
        request.setServletPath("/tenants/shire/login");
        request.addHeader(TenantExtractor.HEADER_TENANT_ID, "shire");
        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(chain.getRequest());
    }
}
