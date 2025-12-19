package org.apereo.cas.rest.audit;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestResponseEntityAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
class RestResponseEntityAuditResourceResolverTests {
    @Test
    void verifyAction() {
        val resolver = new RestResponseEntityAuditResourceResolver(true);
        try (val webServer = new MockWebServer()) {
            webServer.start();
            val headers = new HttpHeaders();
            headers.put("header", CollectionUtils.wrapList("value"));
            headers.put("location", CollectionUtils.wrapList("someplace"));
            val entity = new ResponseEntity<>("The Response Body", headers, HttpStatus.OK);
            assertTrue(resolver.resolveFrom(mock(JoinPoint.class), entity).length > 0);
        }
    }
}
