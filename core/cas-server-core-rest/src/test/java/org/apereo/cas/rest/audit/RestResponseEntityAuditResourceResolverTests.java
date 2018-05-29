package org.apereo.cas.rest.audit;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestResponseEntityAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RestResponseEntityAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        final var r = new RestResponseEntityAuditResourceResolver(true);
        try (var webServer = new MockWebServer(9193)) {
            webServer.start();
            final MultiValueMap headers = new LinkedMultiValueMap();
            headers.put("header", CollectionUtils.wrapList("value"));
            headers.put("location", CollectionUtils.wrapList("someplace"));
            final var entity = new ResponseEntity("The Response Body", headers, HttpStatus.OK);
            assertTrue(r.resolveFrom(mock(JoinPoint.class), entity).length > 0);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
