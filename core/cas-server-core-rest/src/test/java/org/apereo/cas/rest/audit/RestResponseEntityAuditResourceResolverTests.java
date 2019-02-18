package org.apereo.cas.rest.audit;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RestResponseEntityAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
public class RestResponseEntityAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        val r = new RestResponseEntityAuditResourceResolver(true);
        try (val webServer = new MockWebServer(9193)) {
            webServer.start();
            val headers = new LinkedMultiValueMap<String, String>();
            headers.put("header", CollectionUtils.wrapList("value"));
            headers.put("location", CollectionUtils.wrapList("someplace"));
            val entity = new ResponseEntity<String>("The Response Body", headers, HttpStatus.OK);
            assertTrue(r.resolveFrom(mock(JoinPoint.class), entity).length > 0);
        }
    }
}
